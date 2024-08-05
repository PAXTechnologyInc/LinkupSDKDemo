/*
 * ===========================================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) YYYY-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date	                 Author	                Action
 * 2021/01/06  	         Alex           	    Create
 * ===========================================================================================
 */

package com.pax.linkupsdk.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;


public final class WorkExecutor {
    private static final ExecutorService mWorkExecutor;
    private WorkExecutor() {
    }

    static {
        int threadCount = Runtime.getRuntime().availableProcessors();
        mWorkExecutor = new ThreadPoolExecutor(threadCount,threadCount*2,3, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(20));
    }

    public static void execute(@NonNull Runnable command) {
        mWorkExecutor.execute(command);
    }
    public static void release() {
        mWorkExecutor.shutdown();
    }
}
