package id.doja.kitkat.emulator.doja;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class JamDescriptor {
    public String appName = "DoJa App";
    public String appClass = "";
    public String packageUrl = "";
    public String drawArea = "240x240";
    public int spSize = 0;
    public final Map<String, String> properties = new HashMap<String, String>();

    public static JamDescriptor parse(byte[] rawBytes) {
        Charset sjis;
        try {
            sjis = Charset.forName("Shift_JIS");
        } catch (Exception e) {
            sjis = Charset.forName("UTF-8");
        }
        String text = new String(rawBytes, sjis);
        JamDescriptor desc = new JamDescriptor();
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            int eq = trimmed.indexOf('=');
            if (eq > 0) {
                String k = trimmed.substring(0, eq).trim();
                String v = trimmed.substring(eq + 1).trim();
                desc.properties.put(k, v);
            }
        }
        if (desc.properties.containsKey("AppName")) desc.appName = desc.properties.get("AppName");
        if (desc.properties.containsKey("AppClass")) desc.appClass = desc.properties.get("AppClass");
        if (desc.properties.containsKey("PackageURL")) desc.packageUrl = desc.properties.get("PackageURL");
        if (desc.properties.containsKey("DrawArea")) desc.drawArea = desc.properties.get("DrawArea");
        if (desc.properties.containsKey("SPsize")) {
            try {
                desc.spSize = Integer.parseInt(desc.properties.get("SPsize"));
            } catch (NumberFormatException ignored) {}
        }
        return desc;
    }
}
