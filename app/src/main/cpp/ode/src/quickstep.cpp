/*************************************************************************
 *                                                                       *
 * Open Dynamics Engine, Copyright (C) 2001-2003 Russell L. Smith.       *
 * All rights reserved.  Email: russ@q12.org   Web: www.q12.org          *
 *                                                                       *
 * This library is free software; you can redistribute it and/or         *
 * modify it under the terms of EITHER:                                  *
 *   (1) The GNU Lesser General Public License as published by the Free  *
 *       Software Foundation; either version 2.1 of the License, or (at  *
 *       your option) any later version. The text of the GNU Lesser      *
 *       General Public License is included with this library in the     *
 *       file LICENSE.TXT.                                               *
 *   (2) The BSD-style license that is included with this library in     *
 *       the file LICENSE-BSD.TXT.                                       *
 *                                                                       *
 * This library is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the files    *
 * LICENSE.TXT and LICENSE-BSD.TXT for more details.                     *
 *                                                                       *
 *************************************************************************/

#include <ode/common.h>
#include <ode/rotation.h>
#include <ode/timer.h>
#include <ode/error.h>
#include <ode/misc.h>
#include "config.h"
#include "matrix.h"
#include "odemath.h"
#include "objects.h"
#include "joints/joint.h"
#include "lcp.h"
#include "util.h"
#include "threadingutils.h"

#include <new>


#define dMIN(A,B)  ((A)>(B) ? (B) : (A))
#define dMAX(A,B)  ((B)>(A) ? (B) : (A))


//***************************************************************************
// configuration

// for the SOR and CG methods:
// uncomment the following line to use warm starting. this definitely
// help for motor-driven joints. unfortunately it appears to hurt
// with high-friction contacts using the SOR method. use with care

//#define WARM_STARTING 1


// for the SOR method:
// uncomment the following line to determine a new constraint-solving
// order for each iteration. however, the qsort per iteration is expensive,
// and the optimal order is somewhat problem dependent.
// @@@ try the leaf->root ordering.

//#define REORDER_CONSTRAINTS 1


// for the SOR method:
// uncomment the following line to randomly reorder constraint rows
// during the solution. depending on the situation, this can help a lot
// or hardly at all, but it doesn't seem to hurt.

#define RANDOMLY_REORDER_CONSTRAINTS 1

//****************************************************************************
// special matrix multipliers

// multiply block of B matrix (q x 6) with 12 dReal per row with C vektor (q)
static void Multiply1_12q1 (dReal *A, const dReal *B, const dReal *C, unsigned int q)
{
    dIASSERT (q>0 && A && B && C);

    dReal a = 0;
    dReal b = 0;
    dReal c = 0;
    dReal d = 0;
    dReal e = 0;
    dReal f = 0;
    dReal s;

    for(unsigned int i=0, k = 0; i<q; k += 12, i++)
    {
        s = C[i]; //C[i] and B[n+k] cannot overlap because its value has been read into a temporary.

        //For the rest of the loop, the only memory dependency (array) is from B[]
        a += B[  k] * s;
        b += B[1+k] * s;
        c += B[2+k] * s;
        d += B[3+k] * s;
        e += B[4+k] * s;
        f += B[5+k] * s;
    }

    A[0] = a;
    A[1] = b;
    A[2] = c;
    A[3] = d;
    A[4] = e;
    A[5] = f;
}

//***************************************************************************
// testing stuff

#ifdef TIMING
#define IFTIMING(x) x
#else
#define IFTIMING(x) ((void)0)
#endif


struct dJointWithInfo1
{
    dxJoint *joint;
    dxJoint::Info1 info;
};

struct dxQuickStepperStage0Outputs
{
    unsigned int                    nj;
    unsigned int                    m;
    unsigned int                    mfb;
};

struct dxQuickStepperStage1CallContext
{
    void Initialize(const dxStepperProcessingCallContext *stepperCallContext, void *stageMemArenaState, dReal *invI, dJointWithInfo1 *jointinfos)
    {
        m_stepperCallContext = stepperCallContext;
        m_stageMemArenaState = stageMemArenaState; 
        m_invI = invI;
        m_jointinfos = jointinfos;
    }

    const dxStepperProcessingCallContext *m_stepperCallContext;
    void                            *m_stageMemArenaState;
    dReal                           *m_invI;
    dJointWithInfo1                 *m_jointinfos;
    dxQuickStepperStage0Outputs     m_stage0Outputs;
};

struct dxQuickStepperStage0BodiesCallContext
{
    void Initialize(const dxStepperProcessingCallContext *stepperCallContext, dReal *invI)
    {
        m_stepperCallContext = stepperCallContext;
        m_invI = invI;
        m_tagsTaken = 0;
        m_gravityTaken = 0;
        m_inertiaBodyIndex = 0;
    }

    const dxStepperProcessingCallContext *m_stepperCallContext;
    dReal                           *m_invI;
    atomicord32                     m_tagsTaken;
    atomicord32                     m_gravityTaken;
    unsigned int                    volatile m_inertiaBodyIndex;
};

struct dxQuickStepperStage0JointsCallContext
{
    void Initialize(const dxStepperProcessingCallContext *stepperCallContext, dJointWithInfo1 *jointinfos, dxQuickStepperStage0Outputs *stage0Outputs)
    {
        m_stepperCallContext = stepperCallContext;
        m_jointinfos = jointinfos;
        m_stage0Outputs = stage0Outputs;
    }

    const dxStepperProcessingCallContext *m_stepperCallContext;
    dJointWithInfo1                 *m_jointinfos;
    dxQuickStepperStage0Outputs     *m_stage0Outputs;
};

static int dxQuickStepIsland_Stage0_Bodies_Callback(void *callContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee);
static int dxQuickStepIsland_Stage0_Joints_Callback(void *callContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee);
static int dxQuickStepIsland_Stage1_Callback(void *callContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee);

static void dxQuickStepIsland_Stage0_Bodies(dxQuickStepperStage0BodiesCallContext *callContext);
static void dxQuickStepIsland_Stage0_Joints(dxQuickStepperStage0JointsCallContext *callContext);
static void dxQuickStepIsland_Stage1(dxQuickStepperStage1CallContext *callContext);


struct dxQuickStepperLocalContext
{
    void Initialize(dReal *invI, dJointWithInfo1 *jointinfos, unsigned int nj, 
        unsigned int m, unsigned int mfb, const unsigned int *mindex, int *findex, 
        dReal *J, dReal *cfm, dReal *lo, dReal *hi, int *jb, dReal *rhs, dReal *Jcopy)
    {
        m_invI = invI;
        m_jointinfos = jointinfos;
        m_nj = nj;
        m_m = m;
        m_mfb = mfb;
        m_mindex = mindex;
        m_findex = findex; 
        m_J = J;
        m_cfm = cfm;
        m_lo = lo;
        m_hi = hi;
        m_jb = jb;
        m_rhs = rhs;
        m_Jcopy = Jcopy;
    }

    dReal                           *m_invI;
    dJointWithInfo1                 *m_jointinfos;
    unsigned int                    m_nj;
    unsigned int                    m_m;
    unsigned int                    m_mfb;
    const unsigned int              *m_mindex;
    int                             *m_findex;
    dReal                           *m_J;
    dReal                           *m_cfm;
    dReal                           *m_lo;
    dReal                           *m_hi;
    int                             *m_jb;
    dReal                           *m_rhs;
    dReal                           *m_Jcopy;
};

struct dxQuickStepperStage3CallContext
{
    void Initialize(const dxStepperProcessingCallContext *callContext, const dxQuickStepperLocalContext *localContext, 
        void *stage1MemArenaState)
    {
        m_stepperCallContext = callContext;
        m_localContext = localContext;
        m_stage1MemArenaState = stage1MemArenaState;
    }

    const dxStepperProcessingCallContext *m_stepperCallContext;
    const dxQuickStepperLocalContext   *m_localContext;
    void                            *m_stage1MemArenaState;
};

struct dxQuickStepperStage2CallContext
{
    void Initialize(const dxStepperProcessingCallContext *callContext, const dxQuickStepperLocalContext *localContext, 
        dReal *rhs_tmp)
    {
        m_stepperCallContext = callContext;
        m_localContext = localContext;
        m_rhs_tmp = rhs_tmp;
        m_ji_J = 0;
        m_ji_jb = 0;
        m_bi = 0;
        m_Jrhsi = 0;
    }

    const dxStepperProcessingCallContext *m_stepperCallContext;
    const dxQuickStepperLocalContext   *m_localContext;
    dReal                           *m_rhs_tmp;
    volatile unsigned int           m_ji_J;
    volatile unsigned int           m_ji_jb;
    volatile unsigned int           m_bi;
    volatile unsigned int           m_Jrhsi;
};

static int dxQuickStepIsland_Stage2a_Callback(void *callContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee);
static int dxQuickStepIsland_Stage2aSync_Callback(void *callContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee);
static int dxQuickStepIsland_Stage2b_Callback(void *callContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee);
static int dxQuickStepIsland_Stage2bSync_Callback(void *callContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee);
static int dxQuickStepIsland_Stage2c_Callback(void *callContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee);
static int dxQuickStepIsland_Stage3_Callback(void *callContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee);

static void dxQuickStepIsland_Stage2a(dxQuickStepperStage2CallContext *callContext);
static void dxQuickStepIsland_Stage2b(dxQuickStepperStage2CallContext *callContext);
static void dxQuickStepIsland_Stage2c(dxQuickStepperStage2CallContext *callContext);
static void dxQuickStepIsland_Stage3(dxQuickStepperStage3CallContext *callContext);


//***************************************************************************
// various common computations involving the matrix J

// compute iMJ = inv(M)*J'

static void compute_invM_JT (unsigned int m, const dReal *J, dReal *iMJ, int *jb,
                             dxBody * const *body, const dReal *invI)
{
    dReal *iMJ_ptr = iMJ;
    const dReal *J_ptr = J;
    for (unsigned int i=0; i<m; J_ptr += 12, iMJ_ptr += 12, i++) {
        int b1 = jb[(size_t)i*2];
        int b2 = jb[(size_t)i*2+1];
        dReal k1 = body[b1]->invMass;
        for (unsigned int j=0; j<3; j++) iMJ_ptr[j] = k1*J_ptr[j];
        const dReal *invIrow1 = invI + 12*(size_t)(unsigned)b1;
        dMultiply0_331 (iMJ_ptr + 3, invIrow1, J_ptr + 3);
        if (b2 != -1) {
            dReal k2 = body[b2]->invMass;
            for (unsigned int j=0; j<3; j++) iMJ_ptr[j+6] = k2*J_ptr[j+6];
            const dReal *invIrow2 = invI + 12*(size_t)(unsigned)b2;
            dMultiply0_331 (iMJ_ptr + 9, invIrow2, J_ptr + 9);
        }
    }
}

// compute out = inv(M)*J'*in.
#ifdef WARM_STARTING
static void multiply_invM_JT (unsigned int m, unsigned int nb, dReal *iMJ, int *jb,
                              const dReal *in, dReal *out)
{
    dSetZero (out,6*(size_t)nb);
    const dReal *iMJ_ptr = iMJ;
    for (unsigned int i=0; i<m; i++) {
        int b1 = jb[(size_t)i*2];
        int b2 = jb[(size_t)i*2+1];
        const dReal in_i = in[i];
        dReal *out_ptr = out + (size_t)(unsigned)b1*6;
        for (unsigned int j=0; j<6; j++) out_ptr[j] += iMJ_ptr[j] * in_i;
        iMJ_ptr += 6;
        if (b2 != -1) {
            out_ptr = out + (size_t)(unsigned)b2*6;
            for (unsigned int j=0; j<6; j++) out_ptr[j] += iMJ_ptr[j] * in_i;
        }
        iMJ_ptr += 6;
    }
}
#endif

// compute out = J*in.
static void multiplyAdd_J (volatile unsigned *mi_storage, 
    unsigned int m, const dReal *J, const int *jb, const dReal *in, dReal *out)
{
    unsigned int mi;
    while ((mi = ThrsafeIncrementIntUpToLimit(mi_storage, m)) != m) {
        int b1 = jb[(size_t)mi*2];
        int b2 = jb[(size_t)mi*2+1];
        const dReal *J_ptr = J + (size_t)mi * 12;
        dReal sum = REAL(0.0);
        const dReal *in_ptr = in + (size_t)(unsigned)b1*6;
        for (unsigned int j = 0; j < 6; ++j) sum += J_ptr[j] * in_ptr[j];
        if (b2 != -1) {
            in_ptr = in + (size_t)(unsigned)b2*6;
            for (unsigned int j=0; j<6; j++) sum += J_ptr[6 + j] * in_ptr[j];
        }
        out[mi] += sum;
    }
}

// Single threaded versionto be removed later
static void _multiply_J (unsigned int m, const dReal* J, int *jb,
                         const dReal* in, dReal* out)
{
    const dReal* J_ptr = J;
    for (unsigned int i=0; i<m; i++) {
        int b1 = jb[(size_t)i*2];
        int b2 = jb[(size_t)i*2+1];
        dReal sum = 0;
        const dReal* in_ptr = in + (size_t)(unsigned)b1*6;
        for (unsigned int j=0; j<6; j++) sum += J_ptr[j] * in_ptr[j];
        J_ptr += 6;
        if (b2 != -1) {
            in_ptr = in + (size_t)(unsigned)b2*6;
            for (unsigned int j=0; j<6; j++) sum += J_ptr[j] * in_ptr[j];
        }
        J_ptr += 6;
        out[i] = sum;
    }
}


// compute out = (J*inv(M)*J' + cfm)*in.
// use z as an nb*6 temporary.
#ifdef WARM_STARTING
static void multiply_J_invM_JT (unsigned int m, unsigned int nb, dReal *J, dReal *iMJ, int *jb,
                                const dReal *cfm, dReal *z, dReal *in, dReal *out)
{
    multiply_invM_JT (m,nb,iMJ,jb,in,z);
    _multiply_J (m,J,jb,z,out);

    // add cfm
    for (unsigned int i=0; i<m; i++) out[i] += cfm[i] * in[i];
}
#endif

//***************************************************************************
// conjugate gradient method with jacobi preconditioner
// THIS IS EXPERIMENTAL CODE that doesn't work too well, so it is ifdefed out.
//
// adding CFM seems to be critically important to this method.

#ifdef USE_CG_LCP

static inline dReal dot (unsigned int n, const dReal *x, const dReal *y)
{
    dReal sum=0;
    for (unsigned int i=0; i<n; i++) sum += x[i]*y[i];
    return sum;
}


// x = y + z*alpha

static inline void add (unsigned int n, dReal *x, const dReal *y, const dReal *z, dReal alpha)
{
    for (unsigned int i=0; i<n; i++) x[i] = y[i] + z[i]*alpha;
}

static void CG_LCP (dxWorldProcessMemArena *memarena,
                    unsigned int m, unsigned int nb, dReal *J, int *jb, dxBody * const *body,
                    const dReal *invI, dReal *lambda, dReal *fc, dReal *b,
                    dReal *lo, dReal *hi, const dReal *cfm, int *findex,
                    dxQuickStepParameters *qs)
{
    const unsigned int num_iterations = qs->num_iterations;

    // precompute iMJ = inv(M)*J'
    dReal *iMJ = memarena->AllocateArray<dReal> ((size_t)m*12);
    compute_invM_JT (m,J,iMJ,jb,body,invI);

    dReal last_rho = 0;
    dReal *r = memarena->AllocateArray<dReal>(m);
    dReal *z = memarena->AllocateArray<dReal>(m);
    dReal *p = memarena->AllocateArray<dReal>(m);
    dReal *q = memarena->AllocateArray<dReal>(m);

    // precompute 1 / diagonals of A
    dReal *Ad = memarena->AllocateArray<dReal>(m);
    const dReal *iMJ_ptr = iMJ;
    const dReal *J_ptr = J;
    for (unsigned int i=0; i<m; i++) {
        dReal sum = 0;
        for (unsigned int j=0; j<6; j++) sum += iMJ_ptr[j] * J_ptr[j];
        if (jb[(size_t)i*2+1] != -1) {
            for (unsigned int j=6; j<12; j++) sum += iMJ_ptr[j] * J_ptr[j];
        }
        iMJ_ptr += 12;
        J_ptr += 12;
        Ad[i] = REAL(1.0) / (sum + cfm[i]);
    }

#ifdef WARM_STARTING
    // compute residual r = b - A*lambda
    multiply_J_invM_JT (m,nb,J,iMJ,jb,cfm,fc,lambda,r);
    for (unsigned int k=0; k<m; k++) r[k] = b[k] - r[k];
#else
    dSetZero (lambda,m);
    memcpy (r,b,(size_t)m*sizeof(dReal));		// residual r = b - A*lambda
#endif

    for (unsigned int iteration=0; iteration < num_iterations; iteration++) {
        for (unsigned int i=0; i<m; i++) z[i] = r[i]*Ad[i];	// z = inv(M)*r
        dReal rho = dot (m,r,z);		// rho = r'*z

        // @@@
        // we must check for convergence, otherwise rho will go to 0 if
        // we get an exact solution, which will introduce NaNs into the equations.
        if (rho < 1e-10) {
            printf ("CG returned at iteration %d\n",iteration);
            break;
        }

        if (iteration==0) {
            memcpy (p,z,(size_t)m*sizeof(dReal));	// p = z
        }
        else {
            add (m,p,z,p,rho/last_rho);	// p = z + (rho/last_rho)*p
        }

        // compute q = (J*inv(M)*J')*p
        multiply_J_invM_JT (m,nb,J,iMJ,jb,cfm,fc,p,q);

        dReal alpha = rho/dot (m,p,q);		// alpha = rho/(p'*q)
        add (m,lambda,lambda,p,alpha);		// lambda = lambda + alpha*p
        add (m,r,r,q,-alpha);			// r = r - alpha*q
        last_rho = rho;
    }

    // compute fc = inv(M)*J'*lambda
    multiply_invM_JT (m,nb,iMJ,jb,lambda,fc);

#if 0
    // measure solution error
    multiply_J_invM_JT (m,nb,J,iMJ,jb,cfm,fc,lambda,r);
    dReal error = 0;
    for (unsigned int i=0; i<m; i++) error += dFabs(r[i] - b[i]);
    printf ("lambda error = %10.6e\n",error);
#endif
}

#endif

//***************************************************************************
// SOR-LCP method

// nb is the number of bodies in the body array.
// J is an m*12 matrix of constraint rows
// jb is an array of first and second body numbers for each constraint row
// invI is the global frame inverse inertia for each body (stacked 3x3 matrices)
//
// this returns lambda and fc (the constraint force).
// note: fc is returned as inv(M)*J'*lambda, the constraint force is actually J'*lambda
//
// b, lo and hi are modified on exit

struct IndexError {
#ifdef REORDER_CONSTRAINTS
    dReal error;		// error to sort on
    int findex;
#endif
    int index;		// row index
};


#ifdef REORDER_CONSTRAINTS

static int compare_index_error (const void *a, const void *b)
{
    const IndexError *i1 = (IndexError*) a;
    const IndexError *i2 = (IndexError*) b;
    if (i1->findex == -1 && i2->findex != -1) return -1;
    if (i1->findex != -1 && i2->findex == -1) return 1;
    if (i1->error < i2->error) return -1;
    if (i1->error > i2->error) return 1;
    return 0;
}

#endif

static void SOR_LCP (dxWorldProcessMemArena *memarena,
                     const unsigned int m, const unsigned int nb, dReal *J, int *jb, dxBody * const *body,
                     const dReal *invI, dReal *lambda, dReal *fc, dReal *b,
                     const dReal *lo, const dReal *hi, const dReal *cfm, const int *findex,
                     const dxQuickStepParameters *qs)
{
#ifdef WARM_STARTING
    {
        // for warm starting, this seems to be necessary to prevent
        // jerkiness in motor-driven joints. i have no idea why this works.
        for (unsigned int i=0; i<m; i++) lambda[i] *= 0.9;
    }
#else
    dSetZero (lambda,m);
#endif

    // precompute iMJ = inv(M)*J'
    dReal *iMJ = memarena->AllocateArray<dReal>((size_t)m*12);
    compute_invM_JT (m,J,iMJ,jb,body,invI);

    // compute fc=(inv(M)*J')*lambda. we will incrementally maintain fc
    // as we change lambda.
#ifdef WARM_STARTING
    multiply_invM_JT (m,nb,iMJ,jb,lambda,fc);
#else
    dSetZero (fc,(size_t)nb*6);
#endif

    dReal *Ad = memarena->AllocateArray<dReal>(m);

    {
        const dReal sor_w = qs->w;		// SOR over-relaxation parameter
        // precompute 1 / diagonals of A
        const dReal *iMJ_ptr = iMJ;
        const dReal *J_ptr = J;
        for (unsigned int i=0; i<m; J_ptr += 12, iMJ_ptr += 12, i++) {
            dReal sum = 0;
            for (unsigned int j=0; j<6; j++) sum += iMJ_ptr[j] * J_ptr[j];
            if (jb[(size_t)i*2+1] != -1) {
                for (unsigned int k=6; k<12; k++) sum += iMJ_ptr[k] * J_ptr[k];
            }
            Ad[i] = sor_w / (sum + cfm[i]);
        }
    }

    {
        // NOTE: This may seem unnecessary but it's indeed an optimization 
        // to move multiplication by Ad[i] and cfm[i] out of iteration loop.

        // scale J and b by Ad
        dReal *J_ptr = J;
        for (unsigned int i=0; i<m; J_ptr += 12, i++) {
            dReal Ad_i = Ad[i];
            for (unsigned int j=0; j<12; j++) {
                J_ptr[j] *= Ad_i;
            }
            b[i] *= Ad_i;
            // scale Ad by CFM. N.B. this should be done last since it is used above
            Ad[i] = Ad_i * cfm[i];
        }
    }


    // order to solve constraint rows in
    IndexError *order = memarena->AllocateArray<IndexError>(m);
    unsigned int head_size;

#ifndef REORDER_CONSTRAINTS
    {
        // make sure constraints with findex < 0 come first.
        IndexError *orderhead = order, *ordertail = order + (m - 1);

        // Fill the array from both ends
        for (unsigned int i=0; i<m; i++) {
            if (findex[i] == -1) {
                orderhead->index = i; // Place them at the front
                ++orderhead;
            } else {
                ordertail->index = i; // Place them at the end
                --ordertail;
            }
        }
        head_size = (unsigned int)(orderhead - order);
        dIASSERT (orderhead-ordertail==1);
    }
#endif

#ifdef REORDER_CONSTRAINTS
    // the lambda computed at the previous iteration.
    // this is used to measure error for when we are reordering the indexes.
    dReal *last_lambda = memarena->AllocateArray<dReal>(m);
#endif

    const unsigned int num_iterations = qs->num_iterations;
    for (unsigned int iteration=0; iteration < num_iterations; iteration++) {

#ifdef REORDER_CONSTRAINTS
        // constraints with findex == -1 always come first.
        if (iteration < 2) {
            // for the first two iterations, solve the constraints in
            // the given order
            head_size = 0;
            IndexError *ordercurr = order;
            for (unsigned int i = 0; i != m; ordercurr++, i++) {
                int findex_i = findex[i];
                ordercurr->error = i;
                ordercurr->findex = findex_i;
                ordercurr->index = i;
                if (findex_i == -1) { ++head_size; }
            }
        }
        else {
            // sort the constraints so that the ones converging slowest
            // get solved last. use the absolute (not relative) error.
            for (unsigned int i=0; i<m; i++) {
                dReal v1 = dFabs (lambda[i]);
                dReal v2 = dFabs (last_lambda[i]);
                dReal max = (v1 > v2) ? v1 : v2;
                if (max > 0) {
                    //@@@ relative error: order[i].error = dFabs(lambda[i]-last_lambda[i])/max;
                    order[i].error = dFabs(lambda[i]-last_lambda[i]);
                }
                else {
                    order[i].error = dInfinity;
                }
                int findex_i = findex[i];
                order[i].findex = findex_i;
                order[i].index = i;
                if (findex_i == -1) { ++head_size; }
            }
        }
        qsort (order,m,sizeof(IndexError),&compare_index_error);

        //@@@ potential optimization: swap lambda and last_lambda pointers rather
        //    than copying the data. we must make sure lambda is properly
        //    returned to the caller
        memcpy (last_lambda,lambda,(size_t)m*sizeof(dReal));
#endif
#ifdef RANDOMLY_REORDER_CONSTRAINTS
        if ((iteration & 7) == 0) {
            for (unsigned int i=1; i<head_size; i++) {
                int swapi = dRandInt(i+1);
                IndexError tmp = order[i];
                order[i] = order[swapi];
                order[swapi] = tmp;
            }
            unsigned int tail_size = m - head_size;
            for (unsigned int j=1; j<tail_size; j++) {
                int swapj = dRandInt(j+1);
                IndexError tmp = order[head_size + j];
                order[head_size + j] = order[head_size + swapj];
                order[head_size + swapj] = tmp;
            }
        }
#endif

        for (unsigned int i=0; i<m; i++) {
            // @@@ potential optimization: we could pre-sort J and iMJ, thereby
            //     linearizing access to those arrays. hmmm, this does not seem
            //     like a win, but we should think carefully about our memory
            //     access pattern.

            unsigned int index = order[i].index;

            dReal *fc_ptr1;
            dReal *fc_ptr2;
            dReal delta;

            {
                int b1 = jb[(size_t)index*2];
                int b2 = jb[(size_t)index*2+1];
                fc_ptr1 = fc + 6*(size_t)(unsigned)b1;
                fc_ptr2 = (b2 != -1) ? fc + 6*(size_t)(unsigned)b2 : NULL;
            }

            dReal old_lambda = lambda[index];

            {
                delta = b[index] - old_lambda*Ad[index];

                const dReal *J_ptr = J + (size_t)index*12;
                // @@@ potential optimization: SIMD-ize this and the b2 >= 0 case
                delta -=fc_ptr1[0] * J_ptr[0] + fc_ptr1[1] * J_ptr[1] +
                    fc_ptr1[2] * J_ptr[2] + fc_ptr1[3] * J_ptr[3] +
                    fc_ptr1[4] * J_ptr[4] + fc_ptr1[5] * J_ptr[5];
                // @@@ potential optimization: handle 1-body constraints in a separate
                //     loop to avoid the cost of test & jump?
                if (fc_ptr2) {
                    delta -=fc_ptr2[0] * J_ptr[6] + fc_ptr2[1] * J_ptr[7] +
                        fc_ptr2[2] * J_ptr[8] + fc_ptr2[3] * J_ptr[9] +
                        fc_ptr2[4] * J_ptr[10] + fc_ptr2[5] * J_ptr[11];
                }
            }

            {
                dReal hi_act, lo_act;

                // set the limits for this constraint. 
                // this is the place where the QuickStep method differs from the
                // direct LCP solving method, since that method only performs this
                // limit adjustment once per time step, whereas this method performs
                // once per iteration per constraint row.
                // the constraints are ordered so that all lambda[] values needed have
                // already been computed.
                if (findex[index] != -1) {
                    hi_act = dFabs (hi[index] * lambda[findex[index]]);
                    lo_act = -hi_act;
                } else {
                    hi_act = hi[index];
                    lo_act = lo[index];
                }

                // compute lambda and clamp it to [lo,hi].
                // @@@ potential optimization: does SSE have clamping instructions
                //     to save test+jump penalties here?
                dReal new_lambda = old_lambda + delta;
                if (new_lambda < lo_act) {
                    delta = lo_act-old_lambda;
                    lambda[index] = lo_act;
                }
                else if (new_lambda > hi_act) {
                    delta = hi_act-old_lambda;
                    lambda[index] = hi_act;
                }
                else {
                    lambda[index] = new_lambda;
                }
            }

            //@@@ a trick that may or may not help
            //dReal ramp = (1-((dReal)(iteration+1)/(dReal)num_iterations));
            //delta *= ramp;

            {
                const dReal *iMJ_ptr = iMJ + (size_t)index*12;
                // update fc.
                // @@@ potential optimization: SIMD for this and the b2 >= 0 case
                fc_ptr1[0] += delta * iMJ_ptr[0];
                fc_ptr1[1] += delta * iMJ_ptr[1];
                fc_ptr1[2] += delta * iMJ_ptr[2];
                fc_ptr1[3] += delta * iMJ_ptr[3];
                fc_ptr1[4] += delta * iMJ_ptr[4];
                fc_ptr1[5] += delta * iMJ_ptr[5];
                // @@@ potential optimization: handle 1-body constraints in a separate
                //     loop to avoid the cost of test & jump?
                if (fc_ptr2) {
                    fc_ptr2[0] += delta * iMJ_ptr[6];
                    fc_ptr2[1] += delta * iMJ_ptr[7];
                    fc_ptr2[2] += delta * iMJ_ptr[8];
                    fc_ptr2[3] += delta * iMJ_ptr[9];
                    fc_ptr2[4] += delta * iMJ_ptr[10];
                    fc_ptr2[5] += delta * iMJ_ptr[11];
                }
            }
        }
    }
}

/*extern */
void dxQuickStepIsland(const dxStepperProcessingCallContext *callContext)
{
    IFTIMING(dTimerStart("preprocessing"));

    dxWorldProcessMemArena *memarena = callContext->m_stepperArena;
    dxWorld *world = callContext->m_world;
    unsigned int nb = callContext->m_islandBodiesCount;
    unsigned int _nj = callContext->m_islandJointsCount;

    dReal *invI = memarena->AllocateArray<dReal>(3 * 4 * (size_t)nb);
    dJointWithInfo1 *const jointinfos = memarena->AllocateArray<dJointWithInfo1>(_nj);

    const unsigned allowedThreads = callContext->m_stepperAllowedThreads;
    dIASSERT(allowedThreads != 0);

    void *stagesMemArenaState = memarena->SaveState();

    dxQuickStepperStage1CallContext *stage1CallContext = (dxQuickStepperStage1CallContext *)memarena->AllocateBlock(sizeof(dxQuickStepperStage1CallContext));
    stage1CallContext->Initialize(callContext, stagesMemArenaState, invI, jointinfos);

    dxQuickStepperStage0BodiesCallContext *stage0BodiesCallContext = (dxQuickStepperStage0BodiesCallContext *)memarena->AllocateBlock(sizeof(dxQuickStepperStage0BodiesCallContext));
    stage0BodiesCallContext->Initialize(callContext, invI);

    dxQuickStepperStage0JointsCallContext *stage0JointsCallContext = (dxQuickStepperStage0JointsCallContext *)memarena->AllocateBlock(sizeof(dxQuickStepperStage0JointsCallContext));
    stage0JointsCallContext->Initialize(callContext, jointinfos, &stage1CallContext->m_stage0Outputs);

    if (allowedThreads == 1)
    {
        dxQuickStepIsland_Stage0_Bodies(stage0BodiesCallContext);
        dxQuickStepIsland_Stage0_Joints(stage0JointsCallContext);
        dxQuickStepIsland_Stage1(stage1CallContext);
    }
    else
    {
        unsigned bodyThreads = allowedThreads;
        unsigned jointThreads = 1;

        dCallReleaseeID stage1CallReleasee;
        world->PostThreadedCallForUnawareReleasee(NULL, &stage1CallReleasee, bodyThreads + jointThreads, callContext->m_finalReleasee, 
            NULL, &dxQuickStepIsland_Stage1_Callback, stage1CallContext, 0, "QuickStepIsland Stage1");

        world->PostThreadedCallsGroup(NULL, bodyThreads, stage1CallReleasee, &dxQuickStepIsland_Stage0_Bodies_Callback, stage0BodiesCallContext, "QuickStepIsland Stage0-Bodies");

        world->PostThreadedCall(NULL, NULL, 0, stage1CallReleasee, NULL, &dxQuickStepIsland_Stage0_Joints_Callback, stage0JointsCallContext, 0, "QuickStepIsland Stage0-Joints");
        dIASSERT(jointThreads == 1);
    }
}    

static 
int dxQuickStepIsland_Stage0_Bodies_Callback(void *_callContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee)
{
    dxQuickStepperStage0BodiesCallContext *callContext = (dxQuickStepperStage0BodiesCallContext *)_callContext;
    dxQuickStepIsland_Stage0_Bodies(callContext);
    return 1;
}

static 
void dxQuickStepIsland_Stage0_Bodies(dxQuickStepperStage0BodiesCallContext *callContext)
{
    dxBody * const *body = callContext->m_stepperCallContext->m_islandBodiesStart;
    unsigned int nb = callContext->m_stepperCallContext->m_islandBodiesCount;

    if (ThrsafeExchange(&callContext->m_tagsTaken, 1) == 0)
    {
        // number all bodies in the body list - set their tag values
        for (unsigned int i=0; i<nb; i++) body[i]->tag = i;
    }

    if (ThrsafeExchange(&callContext->m_gravityTaken, 1) == 0)
    {
        dxWorld *world = callContext->m_stepperCallContext->m_world;

        // add the gravity force to all bodies
        // since gravity does normally have only one component it's more efficient
        // to run three loops for each individual component
        dxBody *const *const bodyend = body + nb;
        dReal gravity_x = world->gravity[0];
        if (gravity_x) {
            for (dxBody *const *bodycurr = body; bodycurr != bodyend; bodycurr++) {
                dxBody *b = *bodycurr;
                if ((b->flags & dxBodyNoGravity) == 0) {
                    b->facc[0] += b->mass.mass * gravity_x;
                }
            }
        }
        dReal gravity_y = world->gravity[1];
        if (gravity_y) {
            for (dxBody *const *bodycurr = body; bodycurr != bodyend; bodycurr++) {
                dxBody *b = *bodycurr;
                if ((b->flags & dxBodyNoGravity) == 0) {
                    b->facc[1] += b->mass.mass * gravity_y;
                }
            }
        }
        dReal gravity_z = world->gravity[2];
        if (gravity_z) {
            for (dxBody *const *bodycurr = body; bodycurr != bodyend; bodycurr++) {
                dxBody *b = *bodycurr;
                if ((b->flags & dxBodyNoGravity) == 0) {
                    b->facc[2] += b->mass.mass * gravity_z;
                }
            }
        }
    }

    // for all bodies, compute the inertia tensor and its inverse in the global
    // frame, and compute the rotational force and add it to the torque
    // accumulator. I and invI are a vertical stack of 3x4 matrices, one per body.
    {
        dReal *invIrow = callContext->m_invI;
        unsigned int bodyIndex = ThrsafeIncrementIntUpToLimit(&callContext->m_inertiaBodyIndex, nb);

        for (unsigned int i = 0; i != nb; invIrow += 12, ++i) {
            if (i == bodyIndex) {
                dMatrix3 tmp;
                dxBody *b = body[i];

                // compute inverse inertia tensor in global frame
                dMultiply2_333 (tmp,b->invI,b->posr.R);
                dMultiply0_333 (invIrow,b->posr.R,tmp);

                // Don't apply gyroscopic torques to bodies
                // if not flagged or the body is kinematic
                if ((b->flags & dxBodyGyroscopic)&& (b->invMass>0)) {
                    dMatrix3 I;
                    // compute inertia tensor in global frame
                    dMultiply2_333 (tmp,b->mass.I,b->posr.R);
                    dMultiply0_333 (I,b->posr.R,tmp);
                    // compute rotational force
#if 0
                    // Explicit computation
                    dMultiply0_331 (tmp,I,b->avel);
                    dSubtractVectorCross3(b->tacc,b->avel,tmp);
#else
                    // Do the implicit computation based on 
                    //"Stabilizing Gyroscopic Forces in Rigid Multibody Simulations"
                    // (Lacoursière 2006)
                    dReal h = callContext->m_stepperCallContext->m_stepSize; // Step size
                    dVector3 L; // Compute angular momentum
                    dMultiply0_331(L,I,b->avel);
                    
                    // Compute a new effective 'inertia tensor'
                    // for the implicit step: the cross-product 
                    // matrix of the angular momentum plus the
                    // old tensor scaled by the timestep.  
                    // Itild may not be symmetric pos-definite, 
                    // but we can still use it to compute implicit
                    // gyroscopic torques.
                    dMatrix3 Itild={0};  
                    dSetCrossMatrixMinus(Itild,L,4);
                    for (int ii=0;ii<12;++ii) {
                      Itild[ii]=Itild[ii]*h+I[ii];
                    }

                    // Scale momentum by inverse time to get 
                    // a sort of "torque"
                    dScaleVector3(L,dRecip(h)); 
                    // Invert the pseudo-tensor
                    dMatrix3 itInv;
                    // This is a closed-form inversion.
                    // It's probably not numerically stable
                    // when dealing with small masses with
                    // a large asymmetry.
                    // An LU decomposition might be better.
                    if (dInvertMatrix3(itInv,Itild)!=0) {
                        // "Divide" the original tensor
                        // by the pseudo-tensor (on the right)
                        dMultiply0_333(Itild,I,itInv);

                        // This new inertia matrix
                        // rotates the momentum and accumulated
                        // torques to get a new set of torques
                        // that will work correctly when applied
                        // to the old inertia matrix as explicit
                        // torques with a semi-implicit update
                        // step.
                        dVector3 tau0,tau1;
                        dMultiply0_331(tau0,Itild,L);
                        dMultiply0_331(tau1,Itild,b->tacc);

                        dVector3 impTorq;
                        dAddVectors3(impTorq,tau0,tau1);
                        // Pull the initial momentum-torque out
                        // of the equation and replace the
                        // value in the torque accumulator
                        dSubtractVectors3(b->tacc,impTorq,L);
                    }
#endif
                }

                bodyIndex = ThrsafeIncrementIntUpToLimit(&callContext->m_inertiaBodyIndex, nb);
            }
        }
    }
}

static 
int dxQuickStepIsland_Stage0_Joints_Callback(void *_callContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee)
{
    dxQuickStepperStage0JointsCallContext *callContext = (dxQuickStepperStage0JointsCallContext *)_callContext;
    dxQuickStepIsland_Stage0_Joints(callContext);
    return 1;
}

static 
void dxQuickStepIsland_Stage0_Joints(dxQuickStepperStage0JointsCallContext *callContext)
{
    dxJoint * const *_joint = callContext->m_stepperCallContext->m_islandJointsStart;
    unsigned int _nj = callContext->m_stepperCallContext->m_islandJointsCount;

    // get joint information (m = total constraint dimension, nub = number of unbounded variables).
    // joints with m=0 are inactive and are removed from the joints array
    // entirely, so that the code that follows does not consider them.
    {
        unsigned int mcurr = 0, mfbcurr = 0;
        dJointWithInfo1 *jicurr = callContext->m_jointinfos;
        dxJoint *const *const _jend = _joint + _nj;
        for (dxJoint *const *_jcurr = _joint; _jcurr != _jend; _jcurr++) {	// jicurr=dest, _jcurr=src
            dxJoint *j = *_jcurr;
            j->getInfo1 (&jicurr->info);
            dIASSERT (/*jicurr->info.m >= 0 && */jicurr->info.m <= 6 && /*jicurr->info.nub >= 0 && */jicurr->info.nub <= jicurr->info.m);

            unsigned int jm = jicurr->info.m;
            if (jm != 0) {
                mcurr += jm;
                if (j->feedback != NULL) {
                    mfbcurr += jm;
                }
                jicurr->joint = j;
                jicurr++;
            }
        }
        callContext->m_stage0Outputs->nj = jicurr - callContext->m_jointinfos;
        callContext->m_stage0Outputs->m = mcurr;
        callContext->m_stage0Outputs->mfb = mfbcurr;
    }
}

static 
int dxQuickStepIsland_Stage1_Callback(void *_stage1CallContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee)
{
    dxQuickStepperStage1CallContext *stage1CallContext = (dxQuickStepperStage1CallContext *)_stage1CallContext;
    dxQuickStepIsland_Stage1(stage1CallContext);
    return 1;
}

static 
void dxQuickStepIsland_Stage1(dxQuickStepperStage1CallContext *stage1CallContext)
{
    const dxStepperProcessingCallContext *callContext = stage1CallContext->m_stepperCallContext;
    dReal *invI = stage1CallContext->m_invI;
    dJointWithInfo1 *jointinfos = stage1CallContext->m_jointinfos;
    unsigned int nj = stage1CallContext->m_stage0Outputs.nj;
    unsigned int m = stage1CallContext->m_stage0Outputs.m;
    unsigned int mfb = stage1CallContext->m_stage0Outputs.mfb;

    dxWorldProcessMemArena *memarena = callContext->m_stepperArena;
    memarena->RestoreState(stage1CallContext->m_stageMemArenaState);
    stage1CallContext = NULL; // WARNING! _stage1CallContext is not valid after this point!
    dIVERIFY(stage1CallContext == NULL); // To suppress unused variable assignment warnings

    {
        unsigned int _nj = callContext->m_islandJointsCount;
        memarena->ShrinkArray<dJointWithInfo1>(jointinfos, _nj, nj);
    }

    dxWorld *world = callContext->m_world;
    dxBody * const *body = callContext->m_islandBodiesStart;
    unsigned int nb = callContext->m_islandBodiesCount;

    unsigned int *mindex = NULL;
    dReal *J = NULL, *cfm = NULL, *lo = NULL, *hi = NULL, *rhs = NULL, *Jcopy = NULL;
    int *jb = NULL, *findex = NULL;

    // if there are constraints, compute the constraint force
    if (m > 0) {
        mindex = memarena->AllocateArray<unsigned int>(2 * (size_t)(nj + 1));
        {
            unsigned int *mcurr = mindex;
            unsigned int moffs = 0, mfboffs = 0;
            mcurr[0] = moffs;
            mcurr[1] = mfboffs;
            mcurr += 2;

            const dJointWithInfo1 *const jiend = jointinfos + nj;
            for (const dJointWithInfo1 *jicurr = jointinfos; jicurr != jiend; ++jicurr) {
                dxJoint *joint = jicurr->joint;
                moffs += jicurr->info.m;
                if (joint->feedback) { mfboffs += jicurr->info.m; }
                mcurr[0] = moffs;
                mcurr[1] = mfboffs;
                mcurr += 2;
            }
        }

        findex = memarena->AllocateArray<int>(m);
        J = memarena->AllocateArray<dReal>((size_t)m*12);
        cfm = memarena->AllocateArray<dReal>(m);
        lo = memarena->AllocateArray<dReal>(m);
        hi = memarena->AllocateArray<dReal>(m);
        jb = memarena->AllocateArray<int>((size_t)m*2);
        rhs = memarena->AllocateArray<dReal>(m);
        Jcopy = memarena->AllocateArray<dReal>((size_t)mfb*12);
    }

    dxQuickStepperLocalContext *localContext = (dxQuickStepperLocalContext *)memarena->AllocateBlock(sizeof(dxQuickStepperLocalContext));
    localContext->Initialize(invI, jointinfos, nj, m, mfb, mindex, findex, J, cfm, lo, hi, jb, rhs, Jcopy);

    void *stage1MemarenaState = memarena->SaveState();
    dxQuickStepperStage3CallContext *stage3CallContext = (dxQuickStepperStage3CallContext*)memarena->AllocateBlock(sizeof(dxQuickStepperStage3CallContext));
    stage3CallContext->Initialize(callContext, localContext, stage1MemarenaState);

    if (m > 0) {
        // create a constraint equation right hand side vector `rhs', a constraint
        // force mixing vector `cfm', and LCP low and high bound vectors, and an
        // 'findex' vector.
        dReal *rhs_tmp = memarena->AllocateArray<dReal>((size_t)nb*6);

        dxQuickStepperStage2CallContext *stage2CallContext = (dxQuickStepperStage2CallContext*)memarena->AllocateBlock(sizeof(dxQuickStepperStage2CallContext));
        stage2CallContext->Initialize(callContext, localContext, rhs_tmp);

        const unsigned allowedThreads = callContext->m_stepperAllowedThreads;
        dIASSERT(allowedThreads != 0);

        if (allowedThreads == 1)
        {
            dxQuickStepIsland_Stage2a(stage2CallContext);
            dxQuickStepIsland_Stage2b(stage2CallContext);
            dxQuickStepIsland_Stage2c(stage2CallContext);
            dxQuickStepIsland_Stage3(stage3CallContext);
        }
        else
        {
            dCallReleaseeID stage3CallReleasee;
            world->PostThreadedCallForUnawareReleasee(NULL, &stage3CallReleasee, 1, callContext->m_finalReleasee, 
                NULL, &dxQuickStepIsland_Stage3_Callback, stage3CallContext, 0, "QuickStepIsland Stage3");

            dCallReleaseeID stage2bSyncReleasee;
            world->PostThreadedCall(NULL, &stage2bSyncReleasee, 1, stage3CallReleasee, 
                NULL, &dxQuickStepIsland_Stage2bSync_Callback, stage2CallContext, 0, "QuickStepIsland Stage2b Sync");

            dCallReleaseeID stage2aSyncReleasee;
            world->PostThreadedCall(NULL, &stage2aSyncReleasee, allowedThreads, stage2bSyncReleasee, 
                NULL, &dxQuickStepIsland_Stage2aSync_Callback, stage2CallContext, 0, "QuickStepIsland Stage2a Sync");

            world->PostThreadedCallsGroup(NULL, allowedThreads, stage2aSyncReleasee, &dxQuickStepIsland_Stage2a_Callback, stage2CallContext, "QuickStepIsland Stage2a");
        }
    }
    else {
        dxQuickStepIsland_Stage3(stage3CallContext);
    }
}


static 
int dxQuickStepIsland_Stage2a_Callback(void *_stage2CallContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee)
{
    dxQuickStepperStage2CallContext *stage2CallContext = (dxQuickStepperStage2CallContext *)_stage2CallContext;
    dxQuickStepIsland_Stage2a(stage2CallContext);
    return 1;
}

static 
void dxQuickStepIsland_Stage2a(dxQuickStepperStage2CallContext *stage2CallContext)
{
    const dxStepperProcessingCallContext *callContext = stage2CallContext->m_stepperCallContext;
    const dxQuickStepperLocalContext *localContext = stage2CallContext->m_localContext;
    dJointWithInfo1 *jointinfos = localContext->m_jointinfos;
    unsigned int nj = localContext->m_nj;
    const unsigned int *mindex = localContext->m_mindex;

    dxWorld *world = callContext->m_world;
    const dReal stepsizeRecip = dRecip(callContext->m_stepSize);
    {
        int *findex = localContext->m_findex;
        dReal *J = localContext->m_J;
        dReal *cfm = localContext->m_cfm;
        dReal *lo = localContext->m_lo;
        dReal *hi = localContext->m_hi;
        dReal *Jcopy = localContext->m_Jcopy;
        dReal *rhs = localContext->m_rhs;

        IFTIMING (dTimerNow ("create J"));
        // get jacobian data from constraints. an m*12 matrix will be created
        // to store the two jacobian blocks from each constraint. it has this
        // format:
        //
        //   l1 l1 l1 a1 a1 a1 l2 l2 l2 a2 a2 a2 \    .
        //   l1 l1 l1 a1 a1 a1 l2 l2 l2 a2 a2 a2  }-- jacobian for joint 0, body 1 and body 2 (3 rows)
        //   l1 l1 l1 a1 a1 a1 l2 l2 l2 a2 a2 a2 /
        //   l1 l1 l1 a1 a1 a1 l2 l2 l2 a2 a2 a2 }--- jacobian for joint 1, body 1 and body 2 (3 rows)
        //   etc...
        //
        //   (lll) = linear jacobian data
        //   (aaa) = angular jacobian data
        //
        const dReal worldERP = world->global_erp;

        dxJoint::Info2Descr Jinfo;
        Jinfo.rowskip = 12;

        unsigned ji;
        while ((ji = ThrsafeIncrementIntUpToLimit(&stage2CallContext->m_ji_J, nj)) != nj) {
            const unsigned ofsi = mindex[ji * 2 + 0];
            const unsigned int infom = mindex[ji * 2 + 2] - ofsi;

            dReal *const Jrow = J + (size_t)ofsi * 12;
            Jinfo.J1l = Jrow;
            Jinfo.J1a = Jrow + 3;
            Jinfo.J2l = Jrow + 6;
            Jinfo.J2a = Jrow + 9;
            dSetZero (Jrow, infom*12);
            Jinfo.c = rhs + ofsi;
            dSetZero (Jinfo.c, infom);
            Jinfo.cfm = cfm + ofsi;
            dSetValue (Jinfo.cfm, infom, world->global_cfm);
            Jinfo.lo = lo + ofsi;
            dSetValue (Jinfo.lo, infom, -dInfinity);
            Jinfo.hi = hi + ofsi;
            dSetValue (Jinfo.hi, infom, dInfinity);
            Jinfo.findex = findex + ofsi;
            dSetValue(Jinfo.findex, infom, -1);
            
            dxJoint *joint = jointinfos[ji].joint;
            joint->getInfo2(stepsizeRecip, worldERP, &Jinfo);

            dReal *rhs_row = Jinfo.c;
            dReal *cfm_row = Jinfo.cfm;
            for (unsigned int i = 0; i != infom; ++i) {
                rhs_row[i] *= stepsizeRecip;
                cfm_row[i] *= stepsizeRecip;
            }

            // adjust returned findex values for global index numbering
            int *findex_row = Jinfo.findex;
            for (unsigned int j = infom; j != 0; ) {
                --j;
                int fival = findex_row[j];
                if (fival != -1) 
                    findex_row[j] = fival + ofsi;
            }

            // we need a copy of Jacobian for joint feedbacks
            // because it gets destroyed by SOR solver
            // instead of saving all Jacobian, we can save just rows
            // for joints, that requested feedback (which is normally much less)
            unsigned mfbcurr = mindex[ji * 2 + 1], mfbnext = mindex[ji * 2 + 3];
            if (mfbcurr != mfbnext) {
                dReal *Jcopyrow = Jcopy + mfbcurr * 12;
                memcpy(Jcopyrow, Jrow, (mfbnext - mfbcurr) * 12 * sizeof(dReal));
            }
        }
    }

    {
        int *jb = localContext->m_jb;

        // create an array of body numbers for each joint row
        unsigned ji;
        while ((ji = ThrsafeIncrementIntUpToLimit(&stage2CallContext->m_ji_jb, nj)) != nj) {
            dxJoint *joint = jointinfos[ji].joint;
            int b1 = (joint->node[0].body) ? (joint->node[0].body->tag) : -1;
            int b2 = (joint->node[1].body) ? (joint->node[1].body->tag) : -1;

            int *const jb_end = jb + 2 * (size_t)mindex[ji * 2 + 2];
            int *jb_ptr = jb + 2 * (size_t)mindex[ji * 2 + 0];
            for (; jb_ptr != jb_end; jb_ptr += 2) {
                jb_ptr[0] = b1;
                jb_ptr[1] = b2;
            }
        }
    }
}

static 
int dxQuickStepIsland_Stage2aSync_Callback(void *_stage2CallContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee)
{
    dxQuickStepperStage2CallContext *stage2CallContext = (dxQuickStepperStage2CallContext *)_stage2CallContext;
    const dxStepperProcessingCallContext *callContext = stage2CallContext->m_stepperCallContext;
    dxWorld *world = callContext->m_world;
    const unsigned allowedThreads = callContext->m_stepperAllowedThreads;

    world->AlterThreadedCallDependenciesCount(callThisReleasee, allowedThreads);
    world->PostThreadedCallsGroup(NULL, allowedThreads, callThisReleasee, &dxQuickStepIsland_Stage2b_Callback, stage2CallContext, "QuickStepIsland Stage2b");

    return 1;
}

static 
int dxQuickStepIsland_Stage2b_Callback(void *_stage2CallContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee)
{
    dxQuickStepperStage2CallContext *stage2CallContext = (dxQuickStepperStage2CallContext *)_stage2CallContext;
    dxQuickStepIsland_Stage2b(stage2CallContext);
    return 1;
}

static 
void dxQuickStepIsland_Stage2b(dxQuickStepperStage2CallContext *stage2CallContext)
{
    const dxStepperProcessingCallContext *callContext = stage2CallContext->m_stepperCallContext;
    const dxQuickStepperLocalContext *localContext = stage2CallContext->m_localContext;

    const dReal stepsizeRecip = dRecip(callContext->m_stepSize);
    {
        // Warning!!!
        // This code reads facc/tacc fields of body objects which (the fields)
        // may be modified by dxJoint::getInfo2(). Therefore the code must be
        // in different sub-stage from Jacobian construction in Stage2a 
        // to ensure proper synchronization and avoid accessing numbers being modified.
        // Warning!!!
        dxBody * const *const body = callContext->m_islandBodiesStart;
        const unsigned int nb = callContext->m_islandBodiesCount;
        const dReal *invI = localContext->m_invI;
        dReal *rhs_tmp = stage2CallContext->m_rhs_tmp;

        // compute the right hand side `rhs'
        IFTIMING (dTimerNow ("compute rhs_tmp"));

        // put -(v/h + invM*fe) into rhs_tmp
        unsigned bi;
        while ((bi = ThrsafeIncrementIntUpToLimit(&stage2CallContext->m_bi, nb)) != nb) {
            dReal *tmp1curr = rhs_tmp + (size_t)bi * 6;
            const dReal *invIrow = invI + (size_t)bi * (6 * 2);
            dxBody *b = body[bi];
            dReal body_invMass = b->invMass;
            for (unsigned int j=0; j<3; ++j) tmp1curr[j] = -(b->facc[j] * body_invMass + b->lvel[j] * stepsizeRecip);
            dMultiply0_331 (tmp1curr + 3, invIrow, b->tacc);
            for (unsigned int k=0; k<3; ++k) tmp1curr[3+k] = -(b->avel[k] * stepsizeRecip) - tmp1curr[3+k];
        }
    }
}

static 
int dxQuickStepIsland_Stage2bSync_Callback(void *_stage2CallContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee)
{
    dxQuickStepperStage2CallContext *stage2CallContext = (dxQuickStepperStage2CallContext *)_stage2CallContext;
    const dxStepperProcessingCallContext *callContext = stage2CallContext->m_stepperCallContext;
    dxWorld *world = callContext->m_world;
    const unsigned allowedThreads = callContext->m_stepperAllowedThreads;

    world->AlterThreadedCallDependenciesCount(callThisReleasee, allowedThreads);
    world->PostThreadedCallsGroup(NULL, allowedThreads, callThisReleasee, &dxQuickStepIsland_Stage2c_Callback, stage2CallContext, "QuickStepIsland Stage2c");

    return 1;
}


static 
int dxQuickStepIsland_Stage2c_Callback(void *_stage2CallContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee)
{
    dxQuickStepperStage2CallContext *stage2CallContext = (dxQuickStepperStage2CallContext *)_stage2CallContext;
    dxQuickStepIsland_Stage2c(stage2CallContext);
    return 1;
}

static 
void dxQuickStepIsland_Stage2c(dxQuickStepperStage2CallContext *stage2CallContext)
{
    const dxStepperProcessingCallContext *callContext = stage2CallContext->m_stepperCallContext;
    const dxQuickStepperLocalContext *localContext = stage2CallContext->m_localContext;

    const dReal stepsizeRecip = dRecip(callContext->m_stepSize);
    {
        // Warning!!!
        // This code depends on rhs_tmp and therefore must be in different sub-stage 
        // from rhs_tmp calculation in Stage2b to ensure proper synchronization 
        // and avoid accessing numbers being modified.
        // Warning!!!
        dReal *rhs = localContext->m_rhs;
        const dReal *J = localContext->m_J;
        const int *jb = localContext->m_jb;
        const dReal *rhs_tmp = stage2CallContext->m_rhs_tmp;
        const unsigned int m = localContext->m_m;

        // add J*rhs_tmp to rhs
        multiplyAdd_J(&stage2CallContext->m_Jrhsi, m, J, jb, rhs_tmp, rhs);
    }
}


static 
int dxQuickStepIsland_Stage3_Callback(void *_stage3CallContext, dcallindex_t callInstanceIndex, dCallReleaseeID callThisReleasee)
{
    dxQuickStepperStage3CallContext *stage3CallContext = (dxQuickStepperStage3CallContext *)_stage3CallContext;
    dxQuickStepIsland_Stage3(stage3CallContext);
    return 1;
}

static 
void dxQuickStepIsland_Stage3(dxQuickStepperStage3CallContext *stage3CallContext)
{
    const dxStepperProcessingCallContext *callContext = stage3CallContext->m_stepperCallContext;
    const dxQuickStepperLocalContext *localContext = stage3CallContext->m_localContext;

    dxWorldProcessMemArena *memarena = callContext->m_stepperArena;
    memarena->RestoreState(stage3CallContext->m_stage1MemArenaState);
    stage3CallContext = NULL; // WARNING! stage3CallContext is not valid after this point!
    dIVERIFY(stage3CallContext == NULL); // To suppress unused variable assignment warnings

    dReal *invI = localContext->m_invI;
    dJointWithInfo1 *jointinfos = localContext->m_jointinfos;
    unsigned int nj = localContext->m_nj;
    unsigned int m = localContext->m_m;
    unsigned int mfb = localContext->m_mfb;
    const unsigned int *mindex = localContext->m_mindex;
    const int *findex = localContext->m_findex;
    dReal *J = localContext->m_J;
    dReal *cfm = localContext->m_cfm;
    dReal *lo = localContext->m_lo;
    dReal *hi = localContext->m_hi;
    int *jb = localContext->m_jb;
    dReal *rhs = localContext->m_rhs;
    dReal *Jcopy = localContext->m_Jcopy;

    dxWorld *world = callContext->m_world;
    dxBody * const *body = callContext->m_islandBodiesStart;
    unsigned int nb = callContext->m_islandBodiesCount;

    if (m > 0) {
        // load lambda from the value saved on the previous iteration
        dReal *lambda = memarena->AllocateArray<dReal>(m);

#ifdef WARM_STARTING
        {
            dReal *lambdscurr = lambda;
            const dJointWithInfo1 *jicurr = jointinfos;
            const dJointWithInfo1 *const jiend = jicurr + nj;
            for (; jicurr != jiend; jicurr++) {
                unsigned int infom = jicurr->info.m;
                memcpy (lambdscurr, jicurr->joint->lambda, infom * sizeof(dReal));
                lambdscurr += infom;
            }
        }
#endif

        dReal *cforce = memarena->AllocateArray<dReal>((size_t)nb*6);

        BEGIN_STATE_SAVE(memarena, lcpstate) {
            IFTIMING (dTimerNow ("solving LCP problem"));
            // solve the LCP problem and get lambda and invM*constraint_force
            SOR_LCP (memarena,m,nb,J,jb,body,invI,lambda,cforce,rhs,lo,hi,cfm,findex,&world->qs);

        } END_STATE_SAVE(memarena, lcpstate);

#ifdef WARM_STARTING
        {
            // save lambda for the next iteration
            //@@@ note that this doesn't work for contact joints yet, as they are
            // recreated every iteration
            const dReal *lambdacurr = lambda;
            const dJointWithInfo1 *jicurr = jointinfos;
            const dJointWithInfo1 *const jiend = jicurr + nj;
            for (; jicurr != jiend; jicurr++) {
                unsigned int infom = jicurr->info.m;
                memcpy (jicurr->joint->lambda, lambdacurr, infom * sizeof(dReal));
                lambdacurr += infom;
            }
        }
#endif

        // note that the SOR method overwrites rhs and J at this point, so
        // they should not be used again.

        {
            dReal stepsize = callContext->m_stepSize;
            // add stepsize * cforce to the body velocity
            const dReal *cforcecurr = cforce;
            dxBody *const *const bodyend = body + nb;
            for (dxBody *const *bodycurr = body; bodycurr != bodyend; cforcecurr+=6, bodycurr++) {
                dxBody *b = *bodycurr;
                for (unsigned int j=0; j<3; j++) {
                    b->lvel[j] += stepsize * cforcecurr[j];
                    b->avel[j] += stepsize * cforcecurr[3+j];
                }
            }
        }

        if (mfb > 0) {
            // straightforward computation of joint constraint forces:
            // multiply related lambdas with respective J' block for joints
            // where feedback was requested
            dReal data[6];
            const dReal *lambdacurr = lambda;
            const dReal *Jcopyrow = Jcopy;
            const dJointWithInfo1 *jicurr = jointinfos;
            const dJointWithInfo1 *const jiend = jicurr + nj;
            for (; jicurr != jiend; jicurr++) {
                dxJoint *joint = jicurr->joint;
                const unsigned int infom = jicurr->info.m;

                if (joint->feedback) {
                    dJointFeedback *fb = joint->feedback;
                    Multiply1_12q1 (data, Jcopyrow, lambdacurr, infom);
                    fb->f1[0] = data[0];
                    fb->f1[1] = data[1];
                    fb->f1[2] = data[2];
                    fb->t1[0] = data[3];
                    fb->t1[1] = data[4];
                    fb->t1[2] = data[5];

                    if (joint->node[1].body)
                    {
                        Multiply1_12q1 (data, Jcopyrow+6, lambdacurr, infom);
                        fb->f2[0] = data[0];
                        fb->f2[1] = data[1];
                        fb->f2[2] = data[2];
                        fb->t2[0] = data[3];
                        fb->t2[1] = data[4];
                        fb->t2[2] = data[5];
                    }

                    Jcopyrow += infom * 12;
                }

                lambdacurr += infom;
            }
        }
    }

    {
        IFTIMING (dTimerNow ("compute velocity update"));
        dReal stepsize = callContext->m_stepSize;
        // compute the velocity update:
        // add stepsize * invM * fe to the body velocity
        const dReal *invIrow = invI;
        dxBody *const *const bodyend = body + nb;
        for (dxBody *const *bodycurr = body; bodycurr != bodyend; invIrow += 12, bodycurr++) {
            dxBody *b = *bodycurr;
            dReal body_invMass_mul_stepsize = stepsize * b->invMass;
            for (unsigned int j=0; j<3; j++) {
                b->lvel[j] += body_invMass_mul_stepsize * b->facc[j];
                b->tacc[j] *= stepsize;
            }
            dMultiplyAdd0_331 (b->avel, invIrow, b->tacc);
        }
    }

#ifdef CHECK_VELOCITY_OBEYS_CONSTRAINT
    if (m > 0) {
        BEGIN_STATE_SAVE(memarena, velstate) {
            dReal *vel = memarena->AllocateArray<dReal>((size_t)nb*6);

            // check that the updated velocity obeys the constraint (this check needs unmodified J)
            dReal *velcurr = vel;
            dxBody *bodycurr = body, *const bodyend = body + nb;
            for (; bodycurr != bodyend; velcurr += 6, bodycurr++) {
                for (unsigned int j=0; j<3; j++) {
                    velcurr[j] = bodycurr->lvel[j];
                    velcurr[3+j] = bodycurr->avel[j];
                }
            }
            dReal *tmp = memarena->AllocateArray<dReal>(m);
            _multiply_J (m,J,jb,vel,tmp);
            dReal error = 0;
            for (unsigned int i=0; i<m; i++) error += dFabs(tmp[i]);
            printf ("velocity error = %10.6e\n",error);

        } END_STATE_SAVE(memarena, velstate)
    }
#endif

    {
        dReal stepsize = callContext->m_stepSize;
        // update the position and orientation from the new linear/angular velocity
        // (over the given timestep)
        IFTIMING (dTimerNow ("update position"));
        dxBody *const *const bodyend = body + nb;
        for (dxBody *const *bodycurr = body; bodycurr != bodyend; bodycurr++) {
            dxBody *b = *bodycurr;
            dxStepBody (b,stepsize);
        }
    }

    {
        IFTIMING (dTimerNow ("tidy up"));
        // zero all force accumulators
        dxBody *const *const bodyend = body + nb;
        for (dxBody *const *bodycurr = body; bodycurr != bodyend; bodycurr++) {
            dxBody *b = *bodycurr;
            dSetZero (b->facc,3);
            dSetZero (b->tacc,3);
        }
    }

    IFTIMING (dTimerEnd());
    IFTIMING (if (m > 0) dTimerReport (stdout,1));
}

#ifdef USE_CG_LCP
static size_t EstimateGR_LCPMemoryRequirements(unsigned int m)
{
    size_t res = dEFFICIENT_SIZE(sizeof(dReal) * 12 * (size_t)m); // for iMJ
    res += 5 * dEFFICIENT_SIZE(sizeof(dReal) * (size_t)m); // for r, z, p, q, Ad
    return res;
}
#endif

static size_t EstimateSOR_LCPMemoryRequirements(unsigned int m)
{
    size_t res = dEFFICIENT_SIZE(sizeof(dReal) * 12 * (size_t)m); // for iMJ
    res += dEFFICIENT_SIZE(sizeof(dReal) * (size_t)m); // for Ad
    res += dEFFICIENT_SIZE(sizeof(IndexError) * (size_t)m); // for order
#ifdef REORDER_CONSTRAINTS
    res += dEFFICIENT_SIZE(sizeof(dReal) * (size_t)m); // for last_lambda
#endif
    return res;
}

/*extern */
size_t dxEstimateQuickStepMemoryRequirements (
    dxBody * const *body, unsigned int nb, dxJoint * const *_joint, unsigned int _nj)
{
    unsigned int nj, m, mfb;

    {
        unsigned int njcurr = 0, mcurr = 0, mfbcurr = 0;
        dxJoint::SureMaxInfo info;
        dxJoint *const *const _jend = _joint + _nj;
        for (dxJoint *const *_jcurr = _joint; _jcurr != _jend; _jcurr++) {	
            dxJoint *j = *_jcurr;
            j->getSureMaxInfo (&info);

            unsigned int jm = info.max_m;
            if (jm > 0) {
                njcurr++;

                mcurr += jm;
                if (j->feedback)
                    mfbcurr += jm;
            }
        }
        nj = njcurr; m = mcurr; mfb = mfbcurr;
    }

    size_t res = 0;

    res += dEFFICIENT_SIZE(sizeof(dReal) * 3 * 4 * nb); // for invI

    {
        size_t sub1_res1 = dEFFICIENT_SIZE(sizeof(dJointWithInfo1) * _nj); // for initial jointinfos

        size_t sub1_res2 = dEFFICIENT_SIZE(sizeof(dJointWithInfo1) * nj); // for shrunk jointinfos
        sub1_res2 += dEFFICIENT_SIZE(sizeof(dxQuickStepperLocalContext)); // for dxQuickStepLocalContext
        if (m > 0) {
            sub1_res2 += dEFFICIENT_SIZE(sizeof(unsigned int) * 2 * (nj + 1)); // for mindex
            sub1_res2 += dEFFICIENT_SIZE(sizeof(dReal) * 12 * m); // for J
            sub1_res2 += dEFFICIENT_SIZE(sizeof(int) * 2 * m); // for jb
            sub1_res2 += 4 * dEFFICIENT_SIZE(sizeof(dReal) * m); // for cfm, lo, hi, rhs
            sub1_res2 += dEFFICIENT_SIZE(sizeof(int) * m); // for findex
            sub1_res2 += dEFFICIENT_SIZE(sizeof(dReal) * 12 * mfb); // for Jcopy
            {
                size_t sub2_res1 = dEFFICIENT_SIZE(sizeof(dxQuickStepperStage3CallContext)); // for dxQuickStepperStage3CallContext
                sub2_res1 += dEFFICIENT_SIZE(sizeof(dReal) * 6 * nb); // for rhs_tmp
                sub2_res1 += dEFFICIENT_SIZE(sizeof(dxQuickStepperStage2CallContext)); // for dxQuickStepperStage2CallContext

                size_t sub2_res2 = dEFFICIENT_SIZE(sizeof(dReal) * m); // for lambda
                sub2_res2 += dEFFICIENT_SIZE(sizeof(dReal) * 6 * nb); // for cforce
                {
                    size_t sub3_res1 = EstimateSOR_LCPMemoryRequirements(m); // for SOR_LCP

                    size_t sub3_res2 = 0;
#ifdef CHECK_VELOCITY_OBEYS_CONSTRAINT
                    {
                        size_t sub4_res1 = dEFFICIENT_SIZE(sizeof(dReal) * 6 * nb); // for vel
                        sub4_res1 += dEFFICIENT_SIZE(sizeof(dReal) * m); // for tmp

                        size_t sub4_res2 = 0;

                        sub3_res2 += dMAX(sub4_res1, sub4_res2);
                    }
#endif
                    sub2_res2 += dMAX(sub3_res1, sub3_res2);
                }

                sub1_res2 += dMAX(sub2_res1, sub2_res2);
            }
        }
        else {
            sub1_res2 += dEFFICIENT_SIZE(sizeof(dxQuickStepperStage3CallContext)); // for dxQuickStepperStage3CallContext
        }

        size_t sub1_res12_max = dMAX(sub1_res1, sub1_res2);
        size_t stage01_contexts = dEFFICIENT_SIZE(sizeof(dxQuickStepperStage0BodiesCallContext))
            + dEFFICIENT_SIZE(sizeof(dxQuickStepperStage0JointsCallContext))
            + dEFFICIENT_SIZE(sizeof(dxQuickStepperStage1CallContext));
        res += dMAX(sub1_res12_max, stage01_contexts);
    }

    return res;
}

/*extern */
unsigned dxEstimateQuickStepMaxCallCount(
    unsigned activeThreadCount, unsigned allowedThreadCount)
{
    unsigned result = 1 // dxQuickStepIsland itself
        + (2 * allowedThreadCount + 2) // (dxQuickStepIsland_Stage2a + dxQuickStepIsland_Stage2b) * allowedThreadCount + 2 * dxStepIsland_Stage2?_Sync
        + 1; // dxStepIsland_Stage3
    return result;
}

