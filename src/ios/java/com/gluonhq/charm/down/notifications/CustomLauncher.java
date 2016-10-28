/**
 * Copyright (c) 2016, Gluon Software
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse
 *    or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.charm.down.notifications;

import com.gluonhq.charm.down.plugins.LocalNotificationsService;
import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.Preloader;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import org.robovm.apple.foundation.NSDictionary;

/**
 *  Copy of the plugin BasicLauncher
 *  https://bitbucket.org/javafxports/javafxmobile-plugin/src/tip/src/main/resources/ios/sources/BasicLauncher.java
 *  with custom code to process a local notification
 * 
 * Requires setting:
 * launcherClassName = 'com.gluonhq.charm.down.notifications.CustomLauncher'
 * on the build.gradle script
 */
public class CustomLauncher extends UIApplicationDelegateAdapter {

    private static final String IOS_PROPERTY_PREFIX = "ios.";
    private static final String JAVAFX_PLATFORM_PROPERTIES = "javafx.platform.properties";
    private static final String JAVA_CUSTOM_PROPERTIES = "java.custom.properties";

    private static final Class<? extends Application> mainClass = com.gluonhq.otn.OTNApplication.class;
    private static final Class<? extends Preloader> preloaderClass = com.gluonhq.otn.OTNPreloader.class;

    @Override
    public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions launchOptions) {

        Thread launchThread = new Thread() {
            @Override
            public void run() {
                
                String[] args = new String[]{};
                
                // --> Notifications
                if (launchOptions != null && launchOptions.getLocalNotification() != null) {
                    /**
                     * Application launches from a Local Notification, we pass its id as runtime 
                     * parameter
                     */
                    final NSDictionary userInfo = launchOptions.getLocalNotification().getUserInfo();
                    if (userInfo.containsKey("userId")) {
                        String notificationId = "--" + LocalNotificationsService.NOTIFICATION_KEY + 
                                                "=" + userInfo.getString("userId");
                        args = new String[]{ notificationId };
                    }
                }
                // <-- Notifications
                
                if (Application.class.isAssignableFrom(mainClass)) {
                    LauncherImpl.launchApplication(mainClass, preloaderClass, args);
                } else {
                    try {
                        Method mainMethod = mainClass.getMethod("main", new Class<?>[]{(new String[0]).getClass()});
                        mainMethod.invoke(null, new Object[]{args});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        launchThread.setDaemon(true);
        launchThread.start();

        return true;
    }

    public static void main(String[] args) throws Exception {
        InputStream isJavafxPlatformProperties = null;
        try {
            System.out.println("***** didFinishLaunchingWithOptions from CustomLauncher ***");
            isJavafxPlatformProperties = CustomLauncher.class.getResourceAsStream("/" + JAVAFX_PLATFORM_PROPERTIES);
            if (isJavafxPlatformProperties == null) {
                throw new RuntimeException("Could not find /" + JAVAFX_PLATFORM_PROPERTIES + " on classpath.");
            }

            Properties platformProperties = new Properties();
            platformProperties.load(isJavafxPlatformProperties);
            for (Map.Entry<Object, Object> e : platformProperties.entrySet()) {
                String key = (String) e.getKey();
                System.setProperty(key.startsWith(IOS_PROPERTY_PREFIX)
                        ? key.substring(IOS_PROPERTY_PREFIX.length()) : key,
                        (String) e.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Can't load " + JAVAFX_PLATFORM_PROPERTIES, e);
        } finally {
            try {
                if (isJavafxPlatformProperties != null) {
                    isJavafxPlatformProperties.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // try loading java custom properties
        InputStream isJavaCustomProperties = null;
        try {
            isJavaCustomProperties = CustomLauncher.class.getResourceAsStream("/" + JAVA_CUSTOM_PROPERTIES);
            if (isJavaCustomProperties != null) {
                Properties javaCustomProperties = new Properties();
                javaCustomProperties.load(isJavaCustomProperties);
                for (Map.Entry<Object, Object> entry : javaCustomProperties.entrySet()) {
                    System.setProperty((String) entry.getKey(), (String) entry.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (isJavaCustomProperties != null) {
                try {
                    isJavaCustomProperties.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.getProperties().list(System.out);

        try (NSAutoreleasePool pool = new NSAutoreleasePool()) {
            UIApplication.main(args, null, CustomLauncher.class);
        }
    }
}
