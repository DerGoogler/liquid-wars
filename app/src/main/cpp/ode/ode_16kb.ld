SECTIONS {
  . = ALIGN(16384);
  .text : ALIGN(16384) { *(.text*) }
  .data : ALIGN(16384) { *(.data*) }
  .bss : ALIGN(16384) { *(.bss*) }
  .rodata : ALIGN(16384) { *(.rodata*) }
}
