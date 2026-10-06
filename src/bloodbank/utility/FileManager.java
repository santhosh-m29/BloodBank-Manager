package bloodbank.utility;
import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;
import bloodbank.service.SystemState;
public final class FileManager implements AutoCloseable {
    private final Path filePath;
    private FileChannel lockChannel;
    private FileLock lock;
    public FileManager(Path directory) {
        filePath = directory.resolve("system-v2.dat");
    }
    public Path getFilePath() {
        return filePath;
    }
    public void lock() throws IOException {
        Files.createDirectories(filePath.toAbsolutePath().getParent());
        lockChannel = FileChannel.open(filePath.resolveSibling("system.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            lock = lockChannel.tryLock();
        } catch (OverlappingFileLockException ex) {
            lock = null;
        }
        if (lock == null) {
            lockChannel.close();
            throw new IOException("This data directory is already open in another application.");
        }
    }
    public SystemState loadData() throws IOException {
        if (Files.notExists(filePath)) return null;
        try {
            SystemState state = decode(Files.readAllBytes(filePath));
            state.validate();
            return state;
        } catch (RuntimeException ex) {
            throw new IOException("Saved data is invalid; original file was preserved.", ex);
        }
    }
    public void saveData(SystemState data) throws IOException {
        data.validate();
        Files.createDirectories(filePath.toAbsolutePath().getParent());
        Path temp = Files.createTempFile(filePath.toAbsolutePath().getParent(), "snapshot-", ".tmp");
        try {
            byte[] bytes = encode(data);
            try (FileOutputStream out = new FileOutputStream(temp.toFile())) {
                out.write(bytes);
                out.getFD().sync();
            }
            Files.move(temp, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }
    public static SystemState copy(SystemState data) {
        try {
            return decode(encode(data));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to copy application state.", ex);
        }
    }
    private static byte[] encode(SystemState data) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(data);
        }
        return bytes.toByteArray();
    }
    private static SystemState decode(byte[] bytes) throws IOException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            in.setObjectInputFilter(ObjectInputFilter.Config.createFilter("maxdepth=50;maxrefs=1000000;maxbytes=100000000;bloodbank.model.*;bloodbank.service.SystemState;java.base/*;!*"));
            Object value = in.readObject();
            if (!(value instanceof SystemState)) throw new IOException("Unrecognized snapshot format.");
            return (SystemState) value;
        } catch (ClassNotFoundException ex) {
            throw new IOException("Unsupported snapshot class.", ex);
        }
    }
    @Override public void close() throws IOException {
        if (lock != null && lock.isValid()) lock.release();
        if (lockChannel != null) lockChannel.close();
    }
}
