package cmsutil.tools;

/**
 * Вспомогательный класс.
 */
public class Arrays {
    public static void revertArray(byte[] arr, int start, int end) {
        int toRevert = (end - start) / 2 + 1;
        byte tmp;
        if (arr == null)
            return;
        if (start >= end)
            return;
        if (start < 0 || end < 0)
            throw new IllegalArgumentException("Array bounds out of range");
        for (int i = 0; i < toRevert; i++) {
            tmp = arr[start + i];
            arr[start + i] = arr[end - i];
            arr[end - i] = tmp;
        }
    }
}
