package com.example.pca.util;

import com.example.pca.model.IrisDataset;
import com.example.pca.model.PCAResult;
import org.aspectj.lang.JoinPoint;

/** Log mesajlari icin nesneleri kisa ve okunur metne cevirir (buyuk dizileri loga basmaz). */
public final class Describe {

    private Describe() { }

    public static String methodKey(JoinPoint jp) {
        return jp.getSignature().getDeclaringType().getSimpleName() + "." + jp.getSignature().getName();
    }

    public static String args(Object[] args) {
        if (args == null || args.length == 0) return "()";
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(value(args[i]));
        }
        return sb.append(')').toString();
    }

    public static String value(Object o) {
        if (o == null) return "null";
        if (o instanceof double[][] m) {
            return "double[" + m.length + "x" + (m.length > 0 && m[0] != null ? m[0].length : 0) + "]";
        }
        if (o instanceof double[] a) return "double[" + a.length + "]";
        if (o instanceof String[] a) return "String[" + a.length + "]";
        if (o instanceof String s) return "\"" + s + "\"";
        if (o instanceof IrisDataset ds) {
            return "IrisDataset(" + ds.getSampleCount() + " ornek, " + ds.getFeatureCount() + " ozellik)";
        }
        if (o instanceof PCAResult r) {
            return "PCAResult(k=" + r.getComponentCount() + ")";
        }
        String s = String.valueOf(o);
        return s.length() > 80 ? s.substring(0, 77) + "..." : s;
    }
}
