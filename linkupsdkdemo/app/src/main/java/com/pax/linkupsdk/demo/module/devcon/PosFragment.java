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
import android.widget.FrameLayout;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.pax.egarden.devicekit.PrinterHelper;
import com.pax.egarden.devicekit.ScannerHelper;
import com.pax.linkdata.ResultListener;
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

import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;
import static com.pax.linkupsdk.demo.module.devcon.Consts.SKU_MAP;

public class PosFragment extends Fragment {
    private final Context mContext;
    private DeviceHelper mDeviceHelper;
    private PrinterHelper mPrinterHelper;
    CartListener cartListener;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String[] mListInfo = new String[]{
            "Detect Devices",
            "Scan SKU",
            "Payment",
            "Print Receipt",
    };

    public PosFragment(Context context) {
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDeviceHelper = DeviceHelper.getInstance(mContext);
        mPrinterHelper = PrinterHelper.getInstance(mContext);
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
                    print(TransDetail.DEMO_TRANS_DETAIL, true);
                    break;
                default:
                    break;
            }
        });


        return fragmentView;
    }

    private String getTotalItemsPriceStr() {
        double amount = 0;
        List<Item> cartItems = ((HomeActivity) requireActivity()).getCartItems();
        for (Item item : cartItems) {
            amount += Double.parseDouble(item.price);
        }
        return String.valueOf((int) (amount * 100));
    }


    private void print(TransDetail transDetail, boolean isDemo) {
        try {
            CommandChannelRequestContent requestContent = new CommandChannelRequestContent();
            LinkDevice thisDevice = mDeviceHelper.getSelfDeviceInfo();
            Printer printer = getT3180();
            if (printer == null) {
                addLog("No T3180 detected.");
                return;
            }

            String content = generateItemsStr(isDemo);

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
            mPrinterHelper.sendRawCommand(thisDevice.getDeviceID(), thisDevice.getCurrentComponentID(), requestContent);

            requestContent.setCmdRequestData(Base64.encodeToString(content.getBytes("GBK"), Base64.NO_WRAP));
            mPrinterHelper.sendRawCommand(thisDevice.getDeviceID(), thisDevice.getCurrentComponentID(), requestContent);
            requestContent.setLastCmd(true);
            requestContent.setCmdRequestData(Base64.encodeToString(new byte[]{0x0D, 0x0A}, Base64.NO_WRAP));
            mPrinterHelper.sendRawCommand(thisDevice.getDeviceID(), thisDevice.getCurrentComponentID(), requestContent);
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("sendRawCommand failed", e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    ResultListener mListener = new ResultListener() {
        @Override
        public void onSuccess() {
            addLog("ResultListener print success");
        }

        @Override
        public void onFailed(String s) {
            addLog("ResultListener print failed==>" + s);
        }
    };


    private String generateItemsStr(boolean isDemo) {
        if(isDemo){
            return "Item_1 (Demo item 1)) $5.12\nItem_2 (Demo item 2)) $2.56\nItem_3 (Demo item 3)) $2.56\n";
        }

        StringBuilder sb = new StringBuilder();

        List<Item> cartItems = ((HomeActivity) requireActivity()).getCartItems();
        for (int i = 0; i < cartItems.size(); i++) {
            Item item = cartItems.get(i);
            sb.append(item.name);
            sb.append("(");
            sb.append(item.description);
            sb.append(") ");
            sb.append("$" + item.price);
            sb.append("\n");
        }
        return sb.toString();
    }

    private String generateTransDetailStr(TransDetail transDetail) {
        return transDetail.toStringForReceipt();
    }

    private void queryOnlineDeviceList() {
        try {
            List<LinkDevice> linkDeviceList = mDeviceHelper.queryOnlineDeviceList();
            addLog("Total devices detected: " + linkDeviceList.size());
            for (LinkDevice linkDevice : linkDeviceList) {
                StringBuilder scannerStr = new StringBuilder();
                StringBuilder printerStr = new StringBuilder();
                StringBuilder componentStr = new StringBuilder();
                if (linkDevice.isDeviceSelf(getContext())) {
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
                }
                addLog("DeviceName:" + linkDevice.getDeviceName() + " DeviceID:" + linkDevice.getDeviceID() + componentStr);
            }
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("[queryOnlineDeviceList] detect devices failed", e);
        }
    }

    private <T> T findDeviceModel(List<T> devices, String modelName, Function<T, String> getModelFunc) {
        return devices.stream()
                .filter(device -> modelName.equalsIgnoreCase(getModelFunc.apply(device)))
                .findFirst()
                .orElse(null);
    }

    private LinkDevice getA3700() {
        try {
            List<LinkDevice> linkDeviceList = mDeviceHelper.queryOnlineDeviceList();
            return findDeviceModel(linkDeviceList, "A3700", LinkDevice::getDeviceModel);
        } catch (LinkException e) {
            throw new RuntimeException(e);
        }
    }

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

    private void pay() {
        IndicatorUtil.showSpin(requireActivity(), "Processing, please wait.");
        new Thread(this::processPayment).start();
    }

    private void processPayment() {
        try {
            PosLink poslink = initializePosLink();
            if (poslink == null) {
                throw new RuntimeException("POSLink init failed");
            }
            PaymentRequest request = createPaymentRequest();
            poslink.PaymentRequest = request;
            ProcessTransResult processTransResult = poslink.ProcessTrans();

            PaymentResponse paymentResponse = poslink.PaymentResponse;
            // failed
            if(paymentResponse == null){
                IndicatorUtil.hideSpin();
                return;
            }

            Gson gson = new Gson();
            addLog("processTransResult:" + gson.toJson(processTransResult));
            addLog("paymentResponse:" + gson.toJson(paymentResponse));
            Logger logger = Logger.getLogger(PosFragment.class.getName());
            logger.log(Level.INFO, "paymentResponse:" + gson.toJson(paymentResponse));
            handleTransactionResult(paymentResponse);
            IndicatorUtil.hideSpin();

        } catch (Exception e) {
            addLog("Error processing transaction: " + e.getMessage());
        }
    }

    private PosLink initializePosLink() {
        System.out.println("Pressed pay btn");
        LinkDevice a3700 = getA3700();
        if (a3700 == null) {
            addLog("No A3700 detected.");
            return null;
        }

        String ip = a3700.getLinkIP();
        if (ip.isEmpty()) {
            addLog("No IP found");
            return null;
        }
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

    private PaymentRequest createPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        double amount = 0;

        List<Item> cartItems = ((HomeActivity) requireActivity()).getCartItems();
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

    private String generateReferenceNumber() {
        Random random = new Random();
        long min = 100_000_000_000L;
        long max = 999_999_999_999L;
        long range = max - min + 1;
        return String.valueOf(min + (long) (random.nextDouble() * range));
    }

    private void handleTransactionResult(PaymentResponse paymentResponse) {
        if ("000000".equalsIgnoreCase(paymentResponse.ResultCode)) {
            // todo: print receipt
            System.out.println("print receipt");
            List<Item> cartItems = ((HomeActivity) requireActivity()).getCartItems();
            TransDetail transDetail = new TransDetail(getTotalItemsPriceStr(), paymentResponse.ResultCode, paymentResponse.Message, paymentResponse.ApprovedAmount, paymentResponse.BogusAccountNum, paymentResponse.CardType, paymentResponse.HostCode, paymentResponse.RefNum, paymentResponse.Timestamp, cartItems);
            print(transDetail, false);

            cartItems.clear();
            cartListener.onDeleteAll();
        }
    }

    private void scan() {
        try {
            LinkDevice thisDevice = mDeviceHelper.getSelfDeviceInfo();
            Scanner scanner = getT3320();
            if (scanner == null) {
                addLog("No T3200 detected.");
                return;
            }

            ScannerHelper mScannerHelper = ScannerHelper.getInstance(mContext);
            thisDevice.setCurrentComponentID(scanner.getSn());
            System.out.println("device id:" + thisDevice.getDeviceID() + ", scanner id:" + scanner.getSn());

            mScannerHelper.startScan(thisDevice.getDeviceID(), thisDevice.getCurrentComponentID(), 10000, new ScannerDataListener() {
                @Override
                public void onMessage(int code, String message, String listenerOwner) {
                    mainHandler.post(() -> handleScanResult(code, message, listenerOwner));
                }
            });
            addLog("startScan initiated.");
        } catch (LinkException e) {
            addLog("Error scanning: " + e.getMessage());
        }
    }

    private void handleScanResult(int code, String message, String listenerOwner) {
        System.out.println("coonst map: " + SKU_MAP);
        Item item = SKU_MAP.getOrDefault(message, null);
        if (item != null) {
            addLog("Item scanned: " + item);
            cartListener.onItemAdded(item);
        } else {
            addLog("No item matched for scanned code: " + message);
        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            cartListener = (CartListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnItemAddedListener");
        }
    }
}
