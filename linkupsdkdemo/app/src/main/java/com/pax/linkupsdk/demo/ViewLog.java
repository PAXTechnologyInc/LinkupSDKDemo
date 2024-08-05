package com.pax.linkupsdk.demo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.pax.linkdata.cmd.LinkException;

public class ViewLog {

    private static FragmentActivity activity;
    private static Handler mMsgHandler;

    public ViewLog(FragmentActivity activity) {
        this.activity = activity;
//        final TextView log = (TextView) activity.findViewById(R.id.tv_log);
//        log.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                log.setText("");
//                return false;
//            }
//        });
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
    }

    public static void addErrLog(String title, LinkException e) {
        addLog(title + ", error code: " + e.getErrCode() + ", error msg: " + e.getErrMsg());
    }

    public static void addLog(String obj) {
        if (isMainThread()) {
            addLogMain(obj);
        } else {
            display(obj);
        }
    }

    public static void addLogMain(Object obj) {
        String str;
        if (obj instanceof String) {
            str = (String) obj;
        } else {
            byte[] b = (byte[]) obj;
            str = byteArray2String(b);
        }
        if (activity == null) {
            return;
        }
        final TextView log = (TextView) activity.findViewById(R.id.tv_log);
        if (!str.endsWith("\n")) {
            str += "\n";
        }
        log.append(str);

        // Scroll to the bottom.
        final ScrollView scrollView = (ScrollView) activity.findViewById(R.id.scroll_view);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    public void setHandler(Handler handler) {
        this.mMsgHandler = handler;
    }

    public static void display(String str, int arg1) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = str + "\n";
        message.arg1 = arg1;
        mMsgHandler.sendMessage(message);
    }

    public static void display(String str, int arg1, int arg2) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = str + "\n";
        message.arg1 = arg1;
        message.arg2 = arg2;
        mMsgHandler.sendMessage(message);
    }

    public static void display(String str) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = str + "\n";
        mMsgHandler.sendMessage(message);
    }

    /**
     * Convert byte array to string, stop if \0 found.
     *
     * @param buf
     * @return string
     */
    public static String byteArray2String(byte[] buf) {
        return byteArray2String(buf, 0, buf.length);
    }

    /**
     * @param buf
     * @param offset
     * @param len
     * @return
     */
    public static String byteArray2String(byte[] buf, int offset, int len) {
        int count = len;
        for (int i = 0; i < len; ++i) {
            if (buf[offset + i] == 0x00) {
                count = i;
                break;
            }
        }
        return new String(buf, offset, count);
    }

    /**
     * bcd to string
     *
     * @param b bcd array
     * @return string
     */
    public static String bcd2Str(byte[] b) {
        return bcd2Str(b, 0, b.length);
    }

    /**
     * bcd to string
     *
     * @param b     bcd array
     * @param start start offset
     * @param len   convert length
     * @return string
     */
    public static String bcd2Str(byte[] b, int start, int len) {
        char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder sb = new StringBuilder(len * 2);

        for (int i = 0; i < len; ++i) {
            sb.append(HEX_DIGITS[(b[i + start] & 0xF0) >>> 4]);
            sb.append(HEX_DIGITS[b[i + start] & 0x0F]);
        }

        return sb.toString();
    }

    /**
     * byte array to string
     *
     * @param data   byte arrray
     * @param offset convert start offset
     * @param len    convert length
     * @return string
     */
    public static String bytesToStr(byte[] data, int offset, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%c", data[offset + i]));
        }
        return sb.toString();
    }

    /**
     * ascii -> bcd
     *
     * @param srcByte '0'- '9', 'A' - 'F', 'a' - 'f'
     * @return bcd value
     * @example 'A'(0x41) -> 0x0A
     */
    private static byte getCharValue(byte srcByte) {
        int ret;
        if (srcByte >= 'a' && srcByte <= 'f') {
            ret = srcByte - 'a' + 10;
        } else if (srcByte >= 'A' && srcByte <= 'f') {
            ret = srcByte - 'A' + 10;
        } else {
            ret = srcByte - '0';
        }
        return (byte) ret;
    }

    /**
     * string to bcd.
     *
     * @param asc
     * @return bcd array
     * @example "0102" -> 0x01 0x02
     */
    public static byte[] str2Bcd(final String asc) {
        String tmpStr = asc;

        // tmpStr length is even
        if (tmpStr.length() % 2 != 0) {
            tmpStr = asc + "0";
        }

        int bcdLength = tmpStr.length() / 2;
        byte[] bcd = new byte[bcdLength];
        byte[] ascii = tmpStr.getBytes();
        for (int p = 0; p < bcdLength; ++p) {
            bcd[p] = (byte) ((getCharValue(ascii[2 * p]) << 4) + (getCharValue(ascii[2 * p + 1]) & 0xff));
        }
        return bcd;
    }
}
