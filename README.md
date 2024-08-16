This demo app uses the hardware and software stacks of the Elys Solution to demonstrate some basic functionalities of the Linkup SDK. This app provides very basic but very clear implementation of basic POS functions, basic Advertisement functions, and basic Linkup functions.

Requirement: Android Studio, JDK 11 installed.

Attention:
1. Please make sure A3700 is attached to the L1400. We use USB communication to do the POSLink transaction.
2. Please use your own storeId and apiKey to upload menu json file here: public void sendPostRequest(final String storeId, final String apiKey, final String json)
3. If L1400 is not able to detect the printer and scanner, please connect them to the usb hub via usb.
