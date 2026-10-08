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
            public Action decode(RegistryFriendlyByteBuf b){return new Action(b.readUUID(),b.readUtf(32),b.readUtf(16384));}
            public void encode(RegistryFriendlyByteBuf b,Action v){b.writeUUID(v.token);b.writeUtf(v.operation,32);b.writeUtf(v.json,16384);}
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
    public static void register(IEventBus bus){bus.addListener((RegisterPayloadHandlersEvent event)->{
        var channel=event.registrar("manager5");
        channel.playToServer(Request.TYPE,Request.CODEC,(v,c)->c.enqueueWork(()->{if(c.player() instanceof ServerPlayer player)ManagerRuntime.get().request(player,v.section,v.id);}));
        channel.playToServer(Action.TYPE,Action.CODEC,(v,c)->c.enqueueWork(()->{if(c.player() instanceof ServerPlayer player)ManagerRuntime.get().action(player,v);}));
        channel.playToClient(View.TYPE,View.CODEC,(v,c)->c.enqueueWork(()->com.ianblk.zianmanager.client.ManagerClient.accept(v.json)));
    });}
    public static void send(ServerPlayer player,String json){PacketDistributor.sendToPlayer(player,new View(json));}
}
