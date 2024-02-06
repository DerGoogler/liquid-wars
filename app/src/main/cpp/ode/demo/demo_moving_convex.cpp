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

#include <ode/ode.h>
#include <drawstuff/drawstuff.h>
#include "texturepath.h"
#include "bunny_geom.h"
#include "convex_bunny_geom.h"

#ifdef _MSC_VER
#pragma warning(disable:4244 4305)  // for VC++, no precision loss complaints
#endif

// select correct drawing functions

#ifdef dDOUBLE
#define dsDrawBox dsDrawBoxD
#define dsDrawSphere dsDrawSphereD
#define dsDrawCylinder dsDrawCylinderD
#define dsDrawCapsule dsDrawCapsuleD
#define dsDrawLine dsDrawLineD
#define dsDrawTriangle dsDrawTriangleD
#define dsDrawConvex dsDrawConvexD
#endif


// some constants

#define NUM 200			// max number of objects
#define DENSITY (5.0)		// density of all objects
#define GPB 3			// maximum number of geometries per body
#define MAX_CONTACTS 64		// maximum number of contact points per body


// dynamics and collision objects

struct MyObject
{
	dBodyID body;			// the body
	dGeomID geom[GPB];		// geometries representing this body
};

static int num=0;		// number of objects in simulation
static int nextobj=0;		// next object to recycle if num==NUM
static dWorldID world;
static dSpaceID space;
static MyObject obj[NUM];
static dJointGroupID contactgroup;
static int selected = -1;	// selected object
static int show_aabb = 0;	// show geom AABBs?
static int show_contacts = 0;	// show contact points?
static int random_pos = 1;	// drop objects from random position?

typedef dReal dVector3R[3];

// this is called by dSpaceCollide when two objects in space are
// potentially colliding.

static void nearCallback( void *data, dGeomID o1, dGeomID o2 )
{
	int i;
	// if (o1->body && o2->body) return;

	// exit without doing anything if the two bodies are connected by a joint
	dBodyID b1 = dGeomGetBody( o1 );
	dBodyID b2 = dGeomGetBody( o2 );
	if ( b1 && b2 && dAreConnectedExcluding( b1,b2,dJointTypeContact ) ) return;

	dContact contact[MAX_CONTACTS];   // up to MAX_CONTACTS contacts per box-box
	for ( i=0; i<MAX_CONTACTS; i++ )
	{
		contact[i].surface.mode = dContactBounce | dContactSoftCFM;
		contact[i].surface.mu = dInfinity;
		contact[i].surface.mu2 = 0;
		contact[i].surface.bounce = 0.1;
		contact[i].surface.bounce_vel = 0.1;
		contact[i].surface.soft_cfm = 0.01;
	}
	if ( int numc = dCollide( o1,o2,MAX_CONTACTS,&contact[0].geom,
	                          sizeof( dContact ) ) )
	{
		dMatrix3 RI;
		dRSetIdentity( RI );
		const dReal ss[3] = {0.02,0.02,0.02};
		for ( i=0; i<numc; i++ )
		{
			dJointID c = dJointCreateContact( world,contactgroup,contact+i );
			dJointAttach( c,b1,b2 );
			if ( show_contacts ) dsDrawBox( contact[i].geom.pos,RI,ss );
		}
	}
}


// start simulation - set viewpoint

static void start()
{
	dAllocateODEDataForThread( dAllocateMaskAll );

	static float xyz[3] = {2.1640f,-1.3079f,1.7600f};
	static float hpr[3] = {125.5000f,-17.0000f,0.0000f};
	dsSetViewpoint( xyz,hpr );
	printf( "To drop another object, press:\n" );
	printf( "   b for box.\n" );
	printf( "   s for sphere.\n" );
	printf( "   c for capsule.\n" );
	printf( "   v for a convex.\n" );
	printf( "To select an object, press space.\n" );
	printf( "To disable the selected object, press d.\n" );
	printf( "To enable the selected object, press e.\n" );
	printf( "To toggle showing the geom AABBs, press a.\n" );
	printf( "To toggle showing the contact points, press t.\n" );
	printf( "To toggle dropping from random position/orientation, press r.\n" );
}


char locase( char c )
{
	if ( c >= 'A' && c <= 'Z' ) return c - ( 'a'-'A' );
	else return c;
}


// called when a key pressed
static void command( int cmd )
{
	int i,k;
	dReal sides[3];
	dMass m;

	cmd = locase( cmd );
	if ( cmd == 'v' || cmd == 'b' || cmd == 'c' || cmd == 's' || cmd == 'y')
	{
		if ( num < NUM )
		{
			i = num;
			num++;
		}
		else
		{
			i = nextobj;
			nextobj++;
			if ( nextobj >= num ) nextobj = 0;

			// destroy the body and geoms for slot i
			dBodyDestroy( obj[i].body );
			for ( k=0; k < GPB; k++ )
			{
				if ( obj[i].geom[k] ) dGeomDestroy( obj[i].geom[k] );
			}
			memset( &obj[i],0,sizeof( obj[i] ) );
		}

		obj[i].body = dBodyCreate( world );
		for ( k=0; k<3; k++ ) sides[k] = dRandReal()*0.5+0.1;

		dMatrix3 R;
		if ( random_pos )
		{
			dBodySetPosition( obj[i].body,
			                  dRandReal()*2-1,dRandReal()*2-1,dRandReal()+3 );
			dRFromAxisAndAngle( R,dRandReal()*2.0-1.0,dRandReal()*2.0-1.0,
			                    dRandReal()*2.0-1.0,dRandReal()*10.0-5.0 );
		}
		else
		{
			dReal maxheight = 0;
			for ( k=0; k<num; k++ )
			{
				const dReal *pos = dBodyGetPosition( obj[k].body );
				if ( pos[2] > maxheight ) maxheight = pos[2];
			}
			dBodySetPosition( obj[i].body, 0,0,maxheight+1 );
			dRFromAxisAndAngle( R,0,0,1,dRandReal()*10.0-5.0 );
		}
		dBodySetRotation( obj[i].body,R );
		dBodySetData( obj[i].body,( void* )( size_t )i );

		if ( cmd == 'b' )
		{
			dMassSetBox( &m,DENSITY,sides[0],sides[1],sides[2] );
			obj[i].geom[0] = dCreateBox( space,sides[0],sides[1],sides[2] );
		}
		else if ( cmd == 'c' )
		{
			sides[0] *= 0.5;
			dMassSetCapsule( &m,DENSITY,3,sides[0],sides[1] );
			obj[i].geom[0] = dCreateCapsule( space,sides[0],sides[1] );
		}
        else if (cmd == 'y') {
            dMassSetCylinder (&m,DENSITY,3,sides[0],sides[1]);
            obj[i].geom[0] = dCreateCylinder (space,sides[0],sides[1]);
        }
		else if ( cmd == 's' )
		{
			sides[0] *= 0.5;
			dMassSetSphere( &m,DENSITY,sides[0] );
			obj[i].geom[0] = dCreateSphere( space,sides[0] );
		}
		else  if ( cmd == 'v' )
		{
			obj[i].geom[0] = dCreateConvex( space,
			                                convexBunnyPlanes,
			                                convexBunnyPlaneCount,
			                                convexBunnyPoints,
			                                convexBunnyPointCount,
			                                convexBunnyPolygons );

			/// Use equivalent TriMesh to set mass
			dTriMeshDataID new_tmdata = dGeomTriMeshDataCreate();
			dGeomTriMeshDataBuildSingle( new_tmdata, &Vertices[0], 3 * sizeof( float ), VertexCount,
			                             ( dTriIndex* )&Indices[0], IndexCount, 3 * sizeof( dTriIndex ) );

			dGeomID triMesh = dCreateTriMesh( 0, new_tmdata, 0, 0, 0 );

			dMassSetTrimesh( &m, DENSITY, triMesh );

			dGeomDestroy( triMesh );
			dGeomTriMeshDataDestroy( new_tmdata );

			printf( "mass at %f %f %f\n", m.c[0], m.c[1], m.c[2] );
			dGeomSetPosition( obj[i].geom[0], -m.c[0], -m.c[1], -m.c[2] );
			dMassTranslate( &m, -m.c[0], -m.c[1], -m.c[2] );
		}

		for ( k=0; k < GPB; k++ )
		{
			if ( obj[i].geom[k] ) dGeomSetBody( obj[i].geom[k],obj[i].body );
		}

		dBodySetMass( obj[i].body,&m );
	}

	if ( cmd == ' ' )
	{
		selected++;
		if ( selected >= num ) selected = 0;
		if ( selected < 0 ) selected = 0;
	}
	else if ( cmd == 'd' && selected >= 0 && selected < num )
	{
		dBodyDisable( obj[selected].body );
	}
	else if ( cmd == 'e' && selected >= 0 && selected < num )
	{
		dBodyEnable( obj[selected].body );
	}
	else if ( cmd == 'a' )
	{
		show_aabb ^= 1;
	}
	else if ( cmd == 't' )
	{
		show_contacts ^= 1;
	}
	else if ( cmd == 'r' )
	{
		random_pos ^= 1;
	}
}

// draw a geom
void drawGeom( dGeomID g, const dReal *pos, const dReal *R, int show_aabb )
{
	if ( !g ) return;
	if ( !pos ) pos = dGeomGetPosition( g );
	if ( !R ) R = dGeomGetRotation( g );

	int type = dGeomGetClass( g );
	if ( type == dBoxClass )
	{
		dVector3 sides;
		dGeomBoxGetLengths( g,sides );
		dsDrawBox( pos,R,sides );
	}
	else if ( type == dSphereClass )
	{
		dsDrawSphere( pos,R,dGeomSphereGetRadius( g ) );
	}
    else if (type == dCylinderClass) {
        dReal radius,length;
        dGeomCylinderGetParams (g,&radius,&length);
        dsDrawCylinder (pos,R,length,radius);
    }
	else if ( type == dCapsuleClass )
	{
		dReal radius,length;
		dGeomCapsuleGetParams( g,&radius,&length );
		dsDrawCapsule( pos,R,length,radius );
	}
	else if ( type == dConvexClass )
	{
		dsDrawConvex( pos,R,
		              convexBunnyPlanes,
		              convexBunnyPlaneCount,
		              convexBunnyPoints,
		              convexBunnyPointCount,
		              convexBunnyPolygons );
	}

	if ( show_aabb )
	{
		// draw the bounding box for this geom
		dReal aabb[6];
		dGeomGetAABB( g,aabb );
		dVector3 bbpos;
		for ( int i=0; i<3; i++ ) bbpos[i] = 0.5*( aabb[i*2] + aabb[i*2+1] );
		dVector3 bbsides;
		for ( int j=0; j<3; j++ ) bbsides[j] = aabb[j*2+1] - aabb[j*2];
		dMatrix3 RI;
		dRSetIdentity( RI );
		dsSetColorAlpha( 1,0,0,0.5 );
		dsDrawBox( bbpos,RI,bbsides );
	}
}

// simulation loop

static void simLoop( int pause )
{
	dsSetColor( 0,0,2 );
	dSpaceCollide( space,0,&nearCallback );

	if ( !pause ) dWorldQuickStep( world,0.05 );

	for ( int j = 0; j < dSpaceGetNumGeoms( space ); j++ )
	{
		dSpaceGetGeom( space, j );
	}

	// remove all contact joints
	dJointGroupEmpty( contactgroup );

	dsSetColor( 1,1,0 );
	dsSetTexture( DS_WOOD );
	for ( int i=0; i<num; i++ )
	{
		for ( int j=0; j < GPB; j++ )
		{
			if ( obj[i].geom[j] )
			{
				if ( i==selected )
				{
					dsSetColor( 0,0.7,1 );
				}
				else if ( ! dBodyIsEnabled( obj[i].body ) )
				{
					dsSetColor( 1,0,0 );
				}
				else
				{
					dsSetColor( 1,1,0 );
				}

				drawGeom( obj[i].geom[j],0,0,show_aabb );
			}
		}
	}
}


int main( int argc, char **argv )
{
	// setup pointers to drawstuff callback functions
	dsFunctions fn;
	fn.version = DS_VERSION;
	fn.start = &start;
	fn.step = &simLoop;
	fn.command = &command;
	fn.stop = 0;
	fn.path_to_textures = DRAWSTUFF_TEXTURE_PATH;

	// create world
	dInitODE2( 0 );
	world = dWorldCreate();

	space = dSimpleSpaceCreate( 0 );
	contactgroup = dJointGroupCreate( 0 );
	dWorldSetGravity( world,0,0,-0.5 );
	dWorldSetCFM( world,1e-5 );
	dCreatePlane( space,0,0,1,0 );
	memset( obj,0,sizeof( obj ) );

	// run simulation
	dsSimulationLoop( argc,argv,352,288,&fn );

	dJointGroupDestroy( contactgroup );
	dSpaceDestroy( space );
	dWorldDestroy( world );
	dCloseODE();
	return 0;
}

 	  	 
