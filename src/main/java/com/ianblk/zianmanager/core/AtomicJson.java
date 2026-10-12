package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.storage.StorageFiles;
import com.google.gson.*;
import java.nio.file.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
public final class AtomicJson {
    public static final Gson GSON=new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private AtomicJson(){}
    public static <T> T read(Path file,Class<T> type,T fallback) throws IOException{
        if(!StorageFiles.exists(file))return fallback;if(StorageFiles.size(file)>16*1024*1024)throw new IOException("Archivo demasiado grande");
        try(var reader=StorageFiles.newBufferedReader(file,StandardCharsets.UTF_8)){return GSON.fromJson(reader,type);}catch(RuntimeException error){throw new IOException("JSON ilegible; archivo conservado: "+file,error);}
    }
    private static void replace(Path temp,Path file)throws IOException{
        for(int attempt=0;;attempt++)try{StorageFiles.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);return;}catch(AccessDeniedException locked){if(attempt>=4)throw locked;try{Thread.sleep(10L*(attempt+1));}catch(InterruptedException interrupted){Thread.currentThread().interrupt();throw new IOException("Escritura interrumpida",interrupted);}}
    }
    public static void write(Path file,Object value) throws IOException{
        writeEncoded(file,GSON.toJson(value));
    }
    public static void writeEncoded(Path file,String encoded) throws IOException{
        if(encoded.getBytes(StandardCharsets.UTF_8).length>16*1024*1024)throw new IOException("Archivo demasiado grande");
        StorageFiles.createDirectories(file.toAbsolutePath().getParent());Path temp=StorageFiles.createTempFile(file.toAbsolutePath().getParent(),"zianmanager-",".tmp");
        try{StorageFiles.writeString(temp,encoded,StandardCharsets.UTF_8);try(var channel=StorageFiles.channel(temp,StandardOpenOption.WRITE)){channel.force(true);}replace(temp,file);if(!System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("windows"))try(var directory=StorageFiles.channel(file.toAbsolutePath().getParent(),StandardOpenOption.READ)){directory.force(true);}}finally{StorageFiles.deleteIfExists(temp);}
    }
}
