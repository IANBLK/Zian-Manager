package com.ianblk.zianmanager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.UUID;

public final class ManagerNetwork {
    private ManagerNetwork(){}
    public record Request(String section,String id) implements CustomPacketPayload {
        public static final Type<Request> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("zianmanager","request"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Request> CODEC=new StreamCodec<>(){
            public Request decode(RegistryFriendlyByteBuf b){return new Request(b.readUtf(16),b.readUtf(128));}
            public void encode(RegistryFriendlyByteBuf b,Request v){b.writeUtf(v.section,16);b.writeUtf(v.id,128);}
        };
        public Type<Request> type(){return TYPE;}
    }
    public record Action(UUID token,String operation,String json) implements CustomPacketPayload {
        public static final Type<Action> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("zianmanager","action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=new StreamCodec<>(){
            public Action decode(RegistryFriendlyByteBuf b){return new Action(b.readUUID(),b.readUtf(32),b.readUtf(32768));}
            public void encode(RegistryFriendlyByteBuf b,Action v){b.writeUUID(v.token);b.writeUtf(v.operation,32);b.writeUtf(v.json,32768);}
        };
        public Type<Action> type(){return TYPE;}
    }
    public record View(String json) implements CustomPacketPayload {
        public static final Type<View> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("zianmanager","view"));
        public static final StreamCodec<RegistryFriendlyByteBuf,View> CODEC=new StreamCodec<>(){
            public View decode(RegistryFriendlyByteBuf b){return new View(b.readUtf(131072));}
            public void encode(RegistryFriendlyByteBuf b,View v){b.writeUtf(v.json,131072);}
        };
        public Type<View> type(){return TYPE;}
    }
    public record Timer(boolean visible,boolean unlimited,long remaining,int x,int y,String title) implements CustomPacketPayload {
        public static final Type<Timer> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("zianmanager","timer"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Timer> CODEC=new StreamCodec<>(){public Timer decode(RegistryFriendlyByteBuf b){return new Timer(b.readBoolean(),b.readBoolean(),b.readLong(),b.readInt(),b.readInt(),b.readUtf(64));}public void encode(RegistryFriendlyByteBuf b,Timer v){b.writeBoolean(v.visible);b.writeBoolean(v.unlimited);b.writeLong(v.remaining);b.writeInt(v.x);b.writeInt(v.y);b.writeUtf(v.title,64);}};
        public Type<Timer> type(){return TYPE;}
    }
    public static void sendTimer(ServerPlayer player,Timer data){PacketDistributor.sendToPlayer(player,data);}
    public static void register(IEventBus bus){bus.addListener((RegisterPayloadHandlersEvent event)->{
        var channel=event.registrar("manager14");
        channel.playToServer(Request.TYPE,Request.CODEC,(v,c)->c.enqueueWork(()->{if(c.player() instanceof ServerPlayer player)ManagerRuntime.get().request(player,v.section,v.id);}));
        channel.playToServer(Action.TYPE,Action.CODEC,(v,c)->c.enqueueWork(()->{if(c.player() instanceof ServerPlayer player)ManagerRuntime.get().action(player,v);}));
        channel.playToClient(Timer.TYPE,Timer.CODEC,(v,c)->c.enqueueWork(()->com.ianblk.zianmanager.client.DungeonTimerHud.accept(v)));
        channel.playToClient(View.TYPE,View.CODEC,(v,c)->c.enqueueWork(()->com.ianblk.zianmanager.client.ManagerClient.accept(v.json)));
    });}
    public static void send(ServerPlayer player,String json){PacketDistributor.sendToPlayer(player,new View(json));}
}
