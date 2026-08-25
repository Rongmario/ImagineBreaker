package zone.rong.imaginebreaker;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tiny Java 9 class-file emitter for {@link java.util.function.IntSupplier} classes.
 */
final class ClassBytes {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    static String uniqueName() {
        return uniqueName(ClassBytes.class);
    }

    static String uniqueName(Class<?> host) {
        Package pkg = host.getPackage();
        String prefix = pkg == null ? "C" : pkg.getName() + ".C";
        return prefix + COUNTER.incrementAndGet() + "_" + System.nanoTime();
    }

    static byte[] intSupplier(String binaryName, int value) {
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("value must fit in a byte for bipush");
        }
        String internal = binaryName.replace('.', '/');
        ConstantPool cp = new ConstantPool();
        int thisUtf = cp.utf8(internal);
        int thisClass = cp.clazz(thisUtf);
        int objectUtf = cp.utf8("java/lang/Object");
        int objectClass = cp.clazz(objectUtf);
        int ifaceUtf = cp.utf8("java/util/function/IntSupplier");
        int ifaceClass = cp.clazz(ifaceUtf);
        int initUtf = cp.utf8("<init>");
        int voidDesc = cp.utf8("()V");
        int codeUtf = cp.utf8("Code");
        int getUtf = cp.utf8("getAsInt");
        int intDesc = cp.utf8("()I");
        int initNat = cp.nameAndType(initUtf, voidDesc);
        int initRef = cp.method(objectClass, initNat);

        ByteBuffer methods = ByteBuffer.allocate(256);
        // <init>
        methods.putShort((short) 0x0001); // ACC_PUBLIC
        methods.putShort((short) initUtf);
        methods.putShort((short) voidDesc);
        methods.putShort((short) 1); // one attribute: Code
        methods.putShort((short) codeUtf);
        byte[] initCode = new byte[] { 0x2a, (byte) 0xb7, (byte) (initRef >> 8), (byte) initRef, (byte) 0xb1 };
        methods.putInt(2 + 2 + 4 + initCode.length + 2 + 2); // Code attr length
        methods.putShort((short) 1); // max_stack
        methods.putShort((short) 1); // max_locals
        methods.putInt(initCode.length);
        methods.put(initCode);
        methods.putShort((short) 0); // exception table
        methods.putShort((short) 0); // code attributes
        // getAsInt
        methods.putShort((short) 0x0001);
        methods.putShort((short) getUtf);
        methods.putShort((short) intDesc);
        methods.putShort((short) 1);
        methods.putShort((short) codeUtf);
        byte[] body = new byte[] { 0x10, (byte) value, (byte) 0xac }; // bipush, ireturn
        methods.putInt(2 + 2 + 4 + body.length + 2 + 2);
        methods.putShort((short) 1);
        methods.putShort((short) 1);
        methods.putInt(body.length);
        methods.put(body);
        methods.putShort((short) 0);
        methods.putShort((short) 0);
        methods.flip();

        ByteBuffer out = ByteBuffer.allocate(1024);
        out.putInt(0xCAFEBABE);
        out.putShort((short) 0); // minor
        out.putShort((short) 53); // Java 9
        byte[] pool = cp.bytes();
        out.putShort((short) cp.count());
        out.put(pool);
        out.putShort((short) 0x0021); // ACC_PUBLIC ACC_SUPER
        out.putShort((short) thisClass);
        out.putShort((short) objectClass);
        out.putShort((short) 1);
        out.putShort((short) ifaceClass);
        out.putShort((short) 0); // fields
        out.putShort((short) 2); // methods
        byte[] methodBytes = new byte[methods.remaining()];
        methods.get(methodBytes);
        out.put(methodBytes);
        out.putShort((short) 0); // class attributes
        out.flip();
        byte[] result = new byte[out.remaining()];
        out.get(result);
        return result;
    }

    private static final class ConstantPool {

        private final List<byte[]> entries = new ArrayList<>();

        int count() {
            return entries.size() + 1;
        }

        int utf8(String s) {
            byte[] str = s.getBytes(StandardCharsets.UTF_8);
            ByteBuffer b = ByteBuffer.allocate(3 + str.length);
            b.put((byte) 1);
            b.putShort((short) str.length);
            b.put(str);
            return add(b.array());
        }

        int clazz(int utf) {
            return add(new byte[] { 7, (byte) (utf >> 8), (byte) utf });
        }

        int nameAndType(int name, int desc) {
            return add(new byte[] { 12, (byte) (name >> 8), (byte) name, (byte) (desc >> 8), (byte) desc });
        }

        int method(int cls, int nat) {
            return add(new byte[] { 10, (byte) (cls >> 8), (byte) cls, (byte) (nat >> 8), (byte) nat });
        }

        byte[] bytes() {
            int size = 0;
            for (byte[] e : entries) {
                size += e.length;
            }
            ByteBuffer b = ByteBuffer.allocate(size);
            for (byte[] e : entries) {
                b.put(e);
            }
            return b.array();
        }

        private int add(byte[] entry) {
            entries.add(entry);
            return entries.size();
        }

    }

    private ClassBytes() { }

}
