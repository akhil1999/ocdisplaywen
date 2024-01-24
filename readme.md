# OC DISPLAY WEN

Why is it called so? Well, it's an inside joke referring to what people ask ETA alot as in "VoLTE support wen" usually on forums. This is us turning the joke on users hehe.

### Why choose Kotlin? 

Well, recently I went thru Kotlin after developing some time in Java for Android, this project seemed like a good place to practically implement my Kotlin learnings.

## What does this app do?

This app is for testing the AMOLED panel refresh rate limitations.
Currently for ea8076 display panel found on Exynos7885 / 7904 / 9611 Samsung devices circa 2018.

In my experience, ea8076 panel in my Samsung Galaxy M30 (SM-M305F) can clock upto 82Hz stable but there is definitely some color shift.

## How does this work?

On Samsung devices, atleast observed on Exynos7885/7904/9611, there exists a device tree property called "timing,pms" in panel node of display DTSI which is responsible for MIPI clock generation for driving the panel.

A PLL is responsible for generating the MIPI Clock and the frequency is calculated by the formula:

```sh
Fout = ( Mdiv * Fin ) / (Pdiv * (2^Sdiv))
```

Fin is 26 MHz clock which is usually sourced from 52MHz TXCO (temperature compensated) crystal clock divided by two or 26MHz TXCO via a buffer.

the "timing,pms" is usually defined as an array of 3 numbers which maybe in hexadecimal / decimal as 

```sh
timing,pms=<3 127 0>
```

Calculation for the same is 

```sh
Fout = (127 * 26MHz) / (3 * (2 ^ 0))
Fout =  3302 / 3 MHz
Fout ~= 1100.67 MHz
```

Now, stock refresh rate is 60Hz, so 1100MHz MIPI clock delivers 60Hz refresh rate.

Formula for actual refresh rate "ARR" is 

```sh
ARR = ( 60 / MIPI Speed ) * DSI Clock
```

MIPI Speed is the actual FFC speed i.e. Flat Flexible Cable connector from motherboard to display panel determined by a byte sequence located in ea8076_m30_param.h as

```sh
static unsigned char SEQ_FFC_SET[] = {
	0xE9,
	0x11, 0x55, 0xA6, 0x75, 0xA3, 0xB2, 0xA1, 0x4A, 0x00, 0x1A,
	0xB8		/* MIPI Speed 1.1Gbps */
};
```

If we raise the DSI Clock, the rate of frame delivery increases for the MIPI PHY clock resulting in more frames delivered

If we reduce the MIPI Speed, that also results in more frames delivered.

So for MIPI Speed 1.1 Gbps and DSI Clock 1100 MHz or 1.1 GHz means 1 bit transmitted per clock.

Raising DSI Clock to 1.3 Ghz keeping MIPI Speed 1.1Gbps means ~ 1.2 bit(s) transmitted per clock.

Calculation:
```sh
( 60 * 1.3 ) / 1.1 ~= 71 Hz 
```
which is indeed what we get.

A couple of modifications needs to be introduced for device launched with Pie since they have DTBO in separate dedicated partition which is overlaid on top of base DTB, this is a TO-DO.

Exynos7885/7904 devices launched with pre-pie i.e. oreo and earlier use device tree blob inside boot ramdisk, currently this app works with such device i.e. Samsung Galaxy A8, A8+, M30, etc.

## Dependencies / Prebuilt Binaries used:
- [magiskboot](https://github.com/topjohnwu/Magisk)
- [dtbhtool for exynos](https://github.com/akhil1999/dtc-aosp/blob/standalone/dtbtool.c)
- [dtc](https://github.com/akhil1999/dtc-aosp)

A separate repo detailing how static binaries above are built using NDK will be added shortly with steps.

## Credits:
 * [VDavid003](https://github.com/VDavid003) for haccing with me (yes, that's not a typo, it's an inside joke as well)
 * [libxzr](https://github.com/libxzr) who is known for OSS KonaBess from which this app heavily is inspired by, thank you for your work! Would not have been possible without me reading and understanding how KonaBess works!
