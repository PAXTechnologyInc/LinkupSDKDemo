This demo app uses the hardware and software stacks of the Elys Solution to demonstrate some basic functionalities of the Linkup SDK. This app provides very basic but very clear implementation of basic POS functions, basic Advertisement functions, and basic Linkup functions.

## Requirement:
1. Android Studio
2. JDK 11

## Attention:
1. Please make sure A3700, T3180, T3300 are detected by L1400 before everything.
2. Please make sure A3700 is attached to the L1400. We use USB communication to do the POSLink transaction.
3. Please launch BroadPOS (initialize) first on A3700, then can you do POSLink transaction.
4. Please use your own storeId and apiKey to upload menu json file here: public void sendPostRequest(final String storeId, final String apiKey, final String json)
5. If L1400 is not able to detect the printer and scanner, please connect them to the usb hub via usb.
