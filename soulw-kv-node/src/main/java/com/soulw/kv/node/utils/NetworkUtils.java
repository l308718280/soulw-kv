package com.soulw.kv.node.utils;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 21:06
 */
@Slf4j
public class NetworkUtils {

    /**
     * 回环ip
     */
    private static final List<String> CYCLE_IP = ImmutableList.of("127.0.0.1", "localhost", "0.0.0.0");
    private static final Pattern IP_FORMAT = Pattern.compile("\\d+(\\.\\d+){3}");

    public static final String CURRENT_IP = loadIp0();

    /**
     * 获取当前IP
     *
     * @return ip
     */
    public static String loadIp() {
        return CURRENT_IP;
    }

    /**
     * 重新加载IP
     *
     * @return 结果
     */
    public static String loadIp0() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            if (Objects.nonNull(localHost)) {
                String address = localHost.getHostAddress();
                if (IP_FORMAT.matcher(address).matches() && !CYCLE_IP.contains(address)) {
                    return address;
                }
            }

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (Objects.isNull(interfaces)) {
                return null;
            }

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    String addressStr = address.getHostAddress();
                    if (IP_FORMAT.matcher(addressStr).matches() && !CYCLE_IP.contains(addressStr)) {
                        return addressStr;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("load ip error", e);
            return null;
        }
    }
}
