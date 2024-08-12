package com.pax.linkupsdk.demo.module.devcon.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransDetail {
    public String total;
    public String resultCode;
    public String resultMsg;
    public String approvedAmt;
    public String account;
    public String cardType;
    public String hostCode;
    public String refNum;
    public String timeStamp;
    public List<Item> items;


    public TransDetail(String total, String resultCode, String resultMsg, String approvedAmt, String account, String cardType, String hostCode, String refNum, String timeStamp, List<Item> items) {
        this.total = total;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.approvedAmt = approvedAmt;
        this.account = account;
        this.cardType = cardType;
        this.hostCode = hostCode;
        this.refNum = refNum;
        this.timeStamp = timeStamp;
        this.items = items;
    }

    public TransDetail(String total, List<Item> items) {
        this.total = total;
        this.items = items;
    }

    @Override
    public String toString() {
        return "TransDetail{" +
                "\n\ttotal='" + total + '\'' +
                ", \n\tresultCode='" + resultCode + '\'' +
                ", \n\tresultMsg='" + resultMsg + '\'' +
                ", \n\tapprovedAmt='" + approvedAmt + '\'' +
                ", \n\taccount='" + account + '\'' +
                ", \n\tcardType='" + cardType + '\'' +
                ", \n\thostCode='" + hostCode + '\'' +
                ", \n\trefNum='" + refNum + '\'' +
                ", \n\ttimeStamp='" + timeStamp + '\'' +
                ", \n\titems=" + items +
                '}';
    }

    public String toStringForReceipt() {
        return "total: $" + Double.parseDouble(total) / 100.0 +
                ", \nresultCode: " + resultCode +
                ", \nresultMsg: " + resultMsg +
                ", \napprovedAmt: $" + Double.parseDouble(approvedAmt) / 100.0 +
                ", \naccount: " + account +
                ", \ncardType: " + cardType +
                ", \nhostCode: " + hostCode +
                ", \nrefNum: " + refNum +
                ", \ntimeStamp: " + timeStamp;
    }

    static public TransDetail DEMO_TRANS_DETAIL = new TransDetail("1024", "000000", "Demo OK", "1024", "1234", "VISA", "001", "9999", new Date().toString(), new ArrayList<>());
}