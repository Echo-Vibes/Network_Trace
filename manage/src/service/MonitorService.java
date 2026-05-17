package service;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.NetworkIF;
import oshi.hardware.NetworkIF.IfOperStatus;

import java.util.*;

public class MonitorService {

    private final SystemInfo si;
    private long[][] prevTicks;
    private final Map<String, long[]> prevNetStats = new HashMap<>();
    private boolean available = true;

    public MonitorService() {
        SystemInfo tmp;
        try {
            tmp = new SystemInfo();
        } catch (Throwable e) {
            tmp = null;
            available = false;
        }
        si = tmp;
    }

    public synchronized String getMonitorDataJson() {
        if (!available || si == null) {
            return "{\"error\":\"监控服务不可用\"}";
        }

        StringBuilder json = new StringBuilder(1024);
        json.append("{");

        appendCpu(json);
        json.append(",");
        appendMemory(json);
        json.append(",");
        appendNetwork(json);

        json.append("}");
        return json.toString();
    }

    private void appendCpu(StringBuilder json) {
        json.append("\"cpu\":{");
        double percent = -1;
        try {
            CentralProcessor cpu = si.getHardware().getProcessor();
            long[][] ticks = cpu.getProcessorCpuLoadTicks();
            if (prevTicks != null) {
                double[] loads = cpu.getProcessorCpuLoadBetweenTicks(prevTicks);
                double total = 0;
                for (double l : loads) total += l;
                double load = loads.length > 0 ? total / loads.length : 0;
                percent = Math.round(load * 1000.0) / 10.0;
            }
            prevTicks = ticks;
        } catch (Exception e) {
            percent = -1;
        }
        json.append("\"percent\":").append(percent);
        json.append("}");
    }

    private void appendMemory(StringBuilder json) {
        json.append("\"memory\":{");
        try {
            GlobalMemory mem = si.getHardware().getMemory();
            long total = mem.getTotal();
            long available = mem.getAvailable();
            long used = total - available;
            double percent = Math.round((double) used / total * 1000.0) / 10.0;
            json.append("\"total\":").append(total).append(",");
            json.append("\"used\":").append(used).append(",");
            json.append("\"percent\":").append(percent);
        } catch (Exception e) {
            json.append("\"total\":0,\"used\":0,\"percent\":-1");
        }
        json.append("}");
    }

    private void appendNetwork(StringBuilder json) {
        json.append("\"network\":[");
        try {
            List<NetworkIF> nets = si.getHardware().getNetworkIFs();
            long now = System.nanoTime();

            long ethRecv = 0, ethSent = 0;
            long wifiRecv = 0, wifiSent = 0;
            boolean hasEth = false, hasWifi = false;

            for (NetworkIF net : nets) {
                String type = classifyInterface(net);
                if (type == null) continue;

                net.updateAttributes();
                if ("ethernet".equals(type)) {
                    ethRecv += net.getBytesRecv();
                    ethSent += net.getBytesSent();
                    hasEth = true;
                } else {
                    wifiRecv += net.getBytesRecv();
                    wifiSent += net.getBytesSent();
                    hasWifi = true;
                }
            }

            boolean first = true;
            if (hasEth) {
                appendNetItem(json, first, "以太网", "ethernet", ethRecv, ethSent, now);
                first = false;
            }
            if (hasWifi) {
                appendNetItem(json, first, "WiFi", "wifi", wifiRecv, wifiSent, now);
            }
        } catch (Exception e) {
            // network unavailable, return empty
        }
        json.append("]");
    }

    private void appendNetItem(StringBuilder json, boolean first, String name,
                               String type, long recv, long sent, long now) {
        String key = type;
        long[] prev = prevNetStats.get(key);
        double downBps = 0;
        double upBps = 0;

        if (prev != null) {
            long deltaRecv = recv - prev[0];
            long deltaSent = sent - prev[1];
            long deltaNanos = now - prev[2];
            if (deltaRecv < 0) deltaRecv = 0;
            if (deltaSent < 0) deltaSent = 0;
            if (deltaNanos > 0) {
                downBps = (double) deltaRecv * 1_000_000_000 / deltaNanos;
                upBps = (double) deltaSent * 1_000_000_000 / deltaNanos;
            }
        }
        prevNetStats.put(key, new long[]{recv, sent, now});

        if (!first) json.append(",");
        json.append("{\"name\":\"").append(escapeJson(name)).append("\",");
        json.append("\"type\":\"").append(type).append("\",");
        json.append("\"downloadBps\":").append((long) downBps).append(",");
        json.append("\"uploadBps\":").append((long) upBps);
        json.append("}");
    }

    private String classifyInterface(NetworkIF net) {
        String name = net.getName();
        if (name == null) return null;
        String lower = name.toLowerCase();

        // 过滤虚拟/非物理网卡
        if (lower.contains("loopback") || lower.contains("virtual")
                || lower.contains("vmware") || lower.contains("virtualbox")
                || lower.contains("hyper-v") || lower.contains("bluetooth")
                || lower.contains("teredo") || lower.contains("tunnel")
                || lower.contains("pseudo") || lower.contains("vethernet")
                || lower.contains("isatap") || lower.contains("6to4")
                || lower.contains("docker") || lower.contains("wsl")
                || lower.contains("tap-") || lower.contains("zerotier")) {
            return null;
        }

        // 只统计已启用的网卡
        try {
            if (net.getIfOperStatus() != IfOperStatus.UP) {
                return null;
            }
        } catch (Exception ignored) {
        }

        // WiFi 关键词
        if (lower.contains("wlan") || lower.contains("wi-fi") || lower.contains("wifi")
                || lower.contains("wireless") || lower.contains("无线")) {
            return "wifi";
        }

        // 其余物理网卡均视为以太网
        return "ethernet";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append("\\u").append(String.format("%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}