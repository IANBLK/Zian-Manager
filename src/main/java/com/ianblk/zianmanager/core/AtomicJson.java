package com.ianblk.zianmanager.core;
import com.google.gson.*;
import java.nio.file.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
public final class AtomicJson {
    public static final Gson GSON=new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private AtomicJson(){}
    public static <T> T read(Path file,Class<T> type,T fallback) throws IOException{
        if(!Files.exists(file))return fallback;if(Files.size(file)>16*1024*1024)throw new IOException("Archivo demasiado grande");
        try(var reader=Files.newBufferedReader(file,StandardCharsets.UTF_8)){return GSON.fromJson(reader,type);}catch(RuntimeException error){throw new IOException("JSON ilegible; archivo conservado: "+file,error);}
    }
    private static void replace(Path temp,Path file)throws IOException{
        for(int attempt=0;;attempt++)try{Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);return;}catch(AccessDeniedException locked){if(attempt>=4)throw locked;try{Thread.sleep(10L*(attempt+1));}catch(InterruptedException interrupted){Thread.currentThread().interrupt();throw new IOException("Escritura interrumpida",interrupted);}}
    }
    public static void write(Path file,Object value) throws IOException{
        String encoded=GSON.toJson(value);if(encoded.getBytes(StandardCharsets.UTF_8).length>16*1024*1024)throw new IOException("Archivo demasiado grande");
        Files.createDirectories(file.toAbsolutePath().getParent());Path temp=Files.createTempFile(file.toAbsolutePath().getParent(),"zianmanager-",".tmp");
        try{Files.writeString(temp,encoded,StandardCharsets.UTF_8);try(var channel=FileChannel.open(temp,StandardOpenOption.WRITE)){channel.force(true);}replace(temp,file);}finally{Files.deleteIfExists(temp);}
    }
}
