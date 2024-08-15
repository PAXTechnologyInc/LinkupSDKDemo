package com.pax.linkupsdk.demo.module.devcon;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pax.egarden.devicekit.PrinterHelper;
import com.pax.egarden.devicekit.ScannerHelper;
import com.pax.linkdata.ScannerDataListener;
import com.pax.linkdata.cmd.printer.CommandChannelRequestContent;
import com.pax.linkdata.deviceinfo.component.Printer;
import com.pax.linkdata.deviceinfo.component.Scanner;

import com.pax.linkupsdk.demo.HomeActivity;
import com.pax.linkupsdk.demo.R;
import com.pax.egarden.devicekit.DeviceHelper;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.cmd.LinkException;
import com.pax.linkupsdk.demo.module.devcon.models.Item;
import com.pax.linkupsdk.demo.module.devcon.models.TransDetail;
import com.pax.linkupsdk.demo.module.devcon.utils.IndicatorUtil;
import com.pax.poslink.CommSetting;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.constant.EDCType;
import com.pax.poslink.constant.TransType;
import com.pax.poslink.poslink.POSLinkCreator;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;
import static com.pax.linkupsdk.demo.module.devcon.Consts.SKU_MAP;

public class PosFragment extends Fragment {
    private DeviceHelper mDeviceHelper;
    private PrinterHelper mPrinterHelper;
    private ScannerHelper mScannerHelper;
    CartListener cartListener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String[] mListInfo = new String[]{
            "Detect Devices",
            "Scan SKU",
            "Payment",
            "Print Receipt",
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDeviceHelper = DeviceHelper.getInstance(getContext());
        mPrinterHelper = PrinterHelper.getInstance(getContext());
        mScannerHelper = ScannerHelper.getInstance(getContext());

        View fragmentView = inflater.inflate(R.layout.fragment_right, null);
        GridView gridView = fragmentView.findViewById(R.id.gv_function);
        gridView.setAdapter(new ArrayAdapter<String>(requireActivity(), R.layout.gridview_layoutres_btn, mListInfo));
        gridView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (position) {
                case 0:
                    queryOnlineDeviceList();
                    break;
                case 1:
                    scan();
                    break;
                case 2:
                    pay();
                    break;
                case 3:
                    print(TransDetail.DEMO_TRANS_DETAIL, null);
                    break;
                default:
                    break;
            }
        });

        return fragmentView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // set up listener for cart item changing
        try {
            cartListener = (CartListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnItemAddedListener");
        }
    }

    /**
     * Display online linked devices.
     */
    private void queryOnlineDeviceList() {
        try {
            // get linked device list
            List<LinkDevice> linkedDeviceList = mDeviceHelper.queryOnlineDeviceList();
            addLog("Total devices detected: " + linkedDeviceList.size());

            // replace this with your formatted string
            String deviceListStr = generatePrettyDevicesStr(linkedDeviceList);
            addLog(deviceListStr);
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("Fail to detect devices: ", e);
        }
    }

    /**
     * Generate a formatted string of device list
     *
     * @param linkedDeviceList linked device list
     * @return device list string
     */
    private String generatePrettyDevicesStr(List<LinkDevice> linkedDeviceList) {
        StringBuilder sb = new StringBuilder();
        for (LinkDevice linkedDevice : linkedDeviceList) {
            StringBuilder scannerStr = new StringBuilder();
            StringBuilder printerStr = new StringBuilder();
            StringBuilder componentStr = new StringBuilder();
            if (linkedDevice.isDeviceSelf(getContext())) {
                for (Scanner scanner : linkedDevice.getScannerList()) {
                    if (!TextUtils.isEmpty(scanner.getSn())) {
                        scannerStr.append(scanner.getSn());
                        scannerStr.append(";");
                    }
                }
                for (Printer printer : linkedDevice.getPrinterList()) {
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
                sb.append("DeviceName:").append(linkedDevice.getDeviceName()).append(" DeviceID:").append(linkedDevice.getDeviceID()).append(componentStr);
            }
        }
        return sb.toString();
    }

    /**
     * Scan barcode.
     */
    private void scan() {
        try {
            // get L1400
            LinkDevice elys = mDeviceHelper.getSelfDeviceInfo();
            // get scanner
            Scanner scanner = getT3320();
            if (scanner == null) {
                addLog("No T3200 detected.");
                return;
            }

            // start scanning
            IndicatorUtil.showSpin(requireActivity(), "Please scan item.");
            mScannerHelper.startScan(elys.getDeviceID(), scanner.getSn(), 10000, new ScannerDataListener() {
                @Override
                public void onMessage(int code, String message, String listenerOwner) {
                    //  handle scan result
                    mainHandler.post(() -> handleScanResult(message));
                }
            });
        } catch (LinkException e) {
            addLog("Error scanning: " + e.getMessage());
        }
    }

    /**
     * Add scanned item to the cart
     *
     * @param message SKU
     */
    private void handleScanResult(String message) {
        Item item = SKU_MAP.getOrDefault(message, null);
        if (item != null) {
            addLog("Item scanned: " + item);
            cartListener.onItemAdded(item);
        } else {
            addLog("No item matched for scanned code: " + message);
        }
        IndicatorUtil.hideSpin();
    }

    /**
     * Do the payment.
     */
    private void pay() {
        // get item list from HomeActivity
        List<Item> cartItems = ((HomeActivity) requireActivity()).getCartItems();
        // generate request from cart
        PaymentRequest request = createPaymentRequest(cartItems);

        // init POSLink
        PosLink poslink = initializePosLink();
        if (poslink == null) {
            throw new RuntimeException("POSLink init failed");
        }
        poslink.PaymentRequest = request;

        new Thread(() -> {
            requireActivity().runOnUiThread(() -> IndicatorUtil.showSpin(requireActivity(), "Please scan item."));
            // do transaction
            poslink.ProcessTrans();

            // get response
            PaymentResponse paymentResponse = poslink.PaymentResponse;
            // transaction failed
            if (paymentResponse == null) {
                IndicatorUtil.hideSpin();
                return;
            }

            // handle result
            requireActivity().runOnUiThread(() -> {
                handleTransactionResult(paymentResponse, cartItems);
                IndicatorUtil.hideSpin();
            });
        }).start();
    }

    /**
     * POSLink initialization.
     *
     * @return poslink that is ready to do transaction
     */
    private PosLink initializePosLink() {
        // get A3700
        LinkDevice a3700 = getA3700();
        if (a3700 == null) {
            addLog("No A3700 detected.");
            return null;
        }

        // get IP to do the TCP COMM transaction
        String ip = a3700.getLinkIP();
        if (ip.isEmpty()) {
            addLog("No IP found");
            return null;
        }

        // set up POSLink
        PosLink poslink = POSLinkCreator.createPoslink(requireContext());
        CommSetting commSetting = new CommSetting();
        commSetting.setType(CommSetting.TCP);
        commSetting.setDestIP(ip);
        commSetting.setDestPort("10009");
        commSetting.setTimeOut("60000");
        commSetting.setEnableProxy(true);
        poslink.SetCommSetting(commSetting);

        return poslink;
    }

    /**
     * Generate POSLink request from the shopping cart.
     *
     * @return POSLink request
     */
    private PaymentRequest createPaymentRequest(List<Item> cartItems) {
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

    /**
     * Handle result.
     * - Print receipt
     * - Clear cart
     *
     * @param paymentResponse response from POSLink transaction
     */
    private void handleTransactionResult(PaymentResponse paymentResponse, List<Item> cartItems) {
        if ("000000".equalsIgnoreCase(paymentResponse.ResultCode)) {
            TransDetail transDetail = new TransDetail(getTotalItemsPriceStr(cartItems), paymentResponse.ResultCode, paymentResponse.Message, paymentResponse.ApprovedAmount, paymentResponse.BogusAccountNum, paymentResponse.CardType, paymentResponse.HostCode, paymentResponse.RefNum, paymentResponse.Timestamp, cartItems);
            print(transDetail, cartItems);
            cartItems.clear();
            cartListener.onDeleteAll();
        }
    }

    /**
     * Print receipt
     *
     * @param transDetail
     * @param items
     */
    private void print(TransDetail transDetail, List<Item> items) {
        try {
            // generate receipt content
            String content = generateReceiptContent(transDetail, items);

            // get printer
            LinkDevice thisDevice = mDeviceHelper.getSelfDeviceInfo();
            Printer printer = getT3180();
            if (printer == null) {
                addLog("No T3180 detected.");
                return;
            }

            // set up printer
            CommandChannelRequestContent requestContent = configPrinter(content);
            // print
            mPrinterHelper.sendRawCommand(thisDevice.getDeviceID(), printer.getSn(), requestContent);
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("sendRawCommand failed", e);
        }
    }

    /**
     * Generate receipt content string.
     *
     * @param transDetail
     * @param items
     * @return
     */
    private String generateReceiptContent(TransDetail transDetail, List<Item> items) {
        String content = generateItemsStr(items);
        content += "\n\n";
        content += generateTransDetailStr(transDetail);
        content += "\n\n\n\n\n\n\n\n\n\n";
        return content;
    }

    /**
     * Printer configuration
     *
     * @param content receipt data
     * @return requestContent
     */
    private CommandChannelRequestContent configPrinter(final String content) {
        CommandChannelRequestContent requestContent = new CommandChannelRequestContent();
        requestContent.setCmdCaller("DemoTest");
        requestContent.setSendTimeout(10000);
        requestContent.setRecvTimeout(10000);
        requestContent.setLastCmd(false);
        requestContent.setExpRecvLen(0);
        try {
            requestContent.setCmdRequestData(Base64.encodeToString(content.getBytes("GBK"), Base64.NO_WRAP));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return requestContent;
    }

    /**
     * Get total amount in the cart.
     *
     * @return
     */
    private String getTotalItemsPriceStr(List<Item> cartItems) {
        double amount = 0;
        for (Item item : cartItems) {
            amount += Double.parseDouble(item.price);
        }
        return String.valueOf((int) (amount * 100));
    }

    /**
     * Generate customized string of items on the receipt.
     *
     * @param items items on the receipt
     * @return string of item data
     */
    private String generateItemsStr(List<Item> items) {
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
            sb.append("$").append(item.price);
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Get the pretty string of transaction detail on the receipt.
     * @param transDetail
     * @return formatted string
     */
    private String generateTransDetailStr(TransDetail transDetail) {
        return transDetail.toStringForReceipt();
    }

    /**
     * Find certain device based on model among linked devices.
     * @param devices
     * @param modelName
     * @param getModelFunc
     * @return
     * @param <T>
     */
    private <T> T findDeviceModel(List<T> devices, String modelName, Function<T, String> getModelFunc) {
        return devices.stream()
                .filter(device -> modelName.equalsIgnoreCase(getModelFunc.apply(device)))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get A3700
     *
     * @return a3700
     */
    private LinkDevice getA3700() {
        try {
            List<LinkDevice> linkDeviceList = mDeviceHelper.queryOnlineDeviceList();
            return findDeviceModel(linkDeviceList, "A3700", LinkDevice::getDeviceModel);
        } catch (LinkException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get scanner.
     *
     * @return scanner
     */
    private Scanner getT3320() {
        try {
            LinkDevice thisDevice = mDeviceHelper.getSelfDeviceInfo();
            List<Scanner> scannerList = thisDevice.getScannerList();
            if (scannerList.isEmpty()) {
                addLog("Please connect scanner first");
                return null;
            }
            return findDeviceModel(scannerList, "T3320", Scanner::getModel);
        } catch (LinkException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get printer.
     *
     * @return printer
     */
    private Printer getT3180() {
        try {
            LinkDevice thisDevice = mDeviceHelper.getSelfDeviceInfo();
            List<Printer> printerList = thisDevice.getPrinterList();
            if (printerList.isEmpty()) {
                addLog("Please connect printer first");
                return null;
            }
            return findDeviceModel(printerList, "T3180", Printer::getModel);
        } catch (LinkException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate a random ref num for transaction request.
     *
     * @return a ref num
     */
    private String generateReferenceNumber() {
        Random random = new Random();
        long min = 100_000_000_000L;
        long max = 999_999_999_999L;
        long range = max - min + 1;
        return String.valueOf(min + (long) (random.nextDouble() * range));
    }
}
