package com.pax.linkupsdk.demo.module.devcon.utils;

import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;

import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.pax.egarden.devicekit.DeviceHelper;
import com.pax.egarden.devicekit.PrinterHelper;
import com.pax.egarden.sdkimpl.entitys.device.Device;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.cmd.LinkException;
import com.pax.linkdata.cmd.printer.CommandChannelRequestContent;
import com.pax.linkdata.deviceinfo.component.Printer;
import com.pax.linkdata.deviceinfo.component.Scanner;
import com.pax.linkupsdk.demo.HomeActivity;
import com.pax.linkupsdk.demo.module.devcon.PosFragment;
import com.pax.linkupsdk.demo.module.devcon.models.Item;
import com.pax.linkupsdk.demo.module.devcon.models.TransDetail;
import com.pax.poslink.CommSetting;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.constant.EDCType;
import com.pax.poslink.constant.TransType;
import com.pax.poslink.poslink.POSLinkCreator;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LinkupUtil {
    public static List<LinkDevice> getDeviceList(DeviceHelper deviceHelper) {
        try {
            return deviceHelper.queryOnlineDeviceList();
        } catch (LinkException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPrettyDevicesStr(List<LinkDevice> deviceList) {
        StringBuilder sb = new StringBuilder();
        for (LinkDevice linkDevice : deviceList) {
            StringBuilder scannerStr = new StringBuilder();
            StringBuilder printerStr = new StringBuilder();
            StringBuilder componentStr = new StringBuilder();
            for (Scanner scanner : linkDevice.getScannerList()) {
                if (!TextUtils.isEmpty(scanner.getSn())) {
                    scannerStr.append(scanner.getSn());
                    scannerStr.append(";");
                }
            }
            for (Printer printer : linkDevice.getPrinterList()) {
                if (!TextUtils.isEmpty(printer.getSn())) {
                    printerStr.append(printer.getSn());
                    printerStr.append(";");
                }
            }
            if (scannerStr.length() > 0) {
                componentStr.append(" \n\t\tScannerList:").append(scannerStr);
            }
            if (printerStr.length() > 0) {
                componentStr.append(" \n\t\tPrinterList:").append(printerStr);
            }

            sb.append("DeviceName:").append(linkDevice.getDeviceName()).append(" DeviceID:").append(linkDevice.getDeviceID()).append(componentStr);
        }

        return sb.toString();
    }

    public static LinkDevice getThisDevice(DeviceHelper deviceHelper) throws LinkException {
        return deviceHelper.getSelfDeviceInfo();
    }

    private static <T> T findDeviceModel(List<T> devices, String modelName, Function<T, String> getModelFunc) {
        return devices.stream()
                .filter(device -> modelName.equalsIgnoreCase(getModelFunc.apply(device)))
                .findFirst()
                .orElse(null);
    }

    public static LinkDevice getA3700(DeviceHelper deviceHelper) throws LinkException {
        List<LinkDevice> deviceList = deviceHelper.queryOnlineDeviceList();
        return findDeviceModel(deviceList, "A3700", LinkDevice::getDeviceModel);
    }

    private Scanner getT3320(LinkDevice thisDevice) {
        List<Scanner> scannerList = thisDevice.getScannerList();
        if (scannerList.isEmpty()) {
            addLog("Please connect scanner first");
            return null;
        }
        return findDeviceModel(scannerList, "T3320", Scanner::getModel);

    }

    private static Printer getT3180(DeviceHelper deviceHelper) throws LinkException {
        LinkDevice thisDevice = getThisDevice(deviceHelper);
        List<Printer> printerList = thisDevice.getPrinterList();
        if (printerList.isEmpty()) {
            addLog("Please connect printer first");
            return null;
        }
        return findDeviceModel(printerList, "T3180", Printer::getModel);

    }

    public static void print(DeviceHelper deviceHelper, PrinterHelper printerHelper, TransDetail transDetail, List<Item> items) {

        try {
            LinkDevice thisDevice = getThisDevice(deviceHelper);
            Printer printer = getT3180(deviceHelper);
            CommandChannelRequestContent requestContent = new CommandChannelRequestContent();

            if (printer == null) {
                addLog("No T3180 detected.");
                return;
            }

            String content = generateItemsStr(items);

            content += "\n\n";
            content += generateTransDetailStr(transDetail);
            content += "\n\n\n\n\n\n\n\n\n\n";

            String sn = printer.getSn();
            thisDevice.setCurrentComponentID(sn);
            requestContent.setCmdCaller("DemoTest");
            requestContent.setSendTimeout(10000);
            requestContent.setRecvTimeout(10000);
            requestContent.setLastCmd(false);
            requestContent.setExpRecvLen(0);
            requestContent.setCmdRequestData(Base64.encodeToString(new byte[]{0x1B, 0x40}, Base64.NO_WRAP));
            printerHelper.sendRawCommand(thisDevice.getDeviceID(), thisDevice.getCurrentComponentID(), requestContent);

            try {
                requestContent.setCmdRequestData(Base64.encodeToString(content.getBytes("GBK"), Base64.NO_WRAP));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            printerHelper.sendRawCommand(thisDevice.getDeviceID(), thisDevice.getCurrentComponentID(), requestContent);
            requestContent.setLastCmd(true);
            requestContent.setCmdRequestData(Base64.encodeToString(new byte[]{0x0D, 0x0A}, Base64.NO_WRAP));
            printerHelper.sendRawCommand(thisDevice.getDeviceID(), thisDevice.getCurrentComponentID(), requestContent);
        } catch (LinkException e) {
            addErrLog("error print", e);
        }
    }

    static private String generateTransDetailStr(TransDetail transDetail) {
        return transDetail.toStringForReceipt();
    }

    static private String generateItemsStr(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return "Item_1 (Demo item 1)) $5.12\nItem_2 (Demo item 2)) $2.56\nItem_3 (Demo item 3)) $2.56\n";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            sb.append(item.name);
            sb.append("(");
            sb.append(item.description);
            sb.append(") ");
            sb.append("$" + item.price);
            sb.append("\n");
        }
        return sb.toString();
    }


    // payment
    static public PaymentResponse processPayment(DeviceHelper deviceHelper, PosLink posLink, List<Item> cartItems) {
        PosLink poslink = null;
        try {
            poslink = initializePosLink(deviceHelper, posLink);
        } catch (LinkException e) {
            throw new RuntimeException(e);
        }
        poslink.PaymentRequest = createPaymentRequest(cartItems);
        poslink.ProcessTrans();
        return poslink.PaymentResponse;
    }

    static private PosLink initializePosLink(DeviceHelper deviceHelper, PosLink posLink) throws LinkException {
        LinkDevice a3700 = LinkupUtil.getA3700(deviceHelper);
        String ip = a3700.getLinkIP();
        CommSetting commSetting = new CommSetting();
        commSetting.setType(CommSetting.TCP);
        commSetting.setDestIP(ip);
        commSetting.setDestPort("10009");
        commSetting.setTimeOut("60000");
        commSetting.setEnableProxy(true);
        posLink.SetCommSetting(commSetting);
        return posLink;
    }

    static private PaymentRequest createPaymentRequest(List<Item> cartItems) {
        PaymentRequest request = new PaymentRequest();
        double amount = 0;

        for (Item item : cartItems) {
            amount += Double.parseDouble(item.price);
        }
        request.Amount = String.valueOf((int) (amount * 100));
        request.TenderType = request.ParseTenderType(EDCType.CREDIT);
        request.TransType = request.ParseTransType(TransType.SALE);
        request.TipAmt = "0";
        request.TaxAmt = "0";
        request.ECRRefNum = generateReferenceNumber();
        return request;
    }

    static private String generateReferenceNumber() {
        Random random = new Random();
        long min = 100_000_000_000L;
        long max = 999_999_999_999L;
        long range = max - min + 1;
        return String.valueOf(min + (long) (random.nextDouble() * range));
    }
}
