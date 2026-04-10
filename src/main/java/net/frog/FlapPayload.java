package net.frog;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FlapPayload() implements CustomPayload {
    public static final Id<FlapPayload> ID = new Id<>(Identifier.of("icarus-wings", "flap"));
    public static final PacketCodec<PacketByteBuf, FlapPayload> CODEC =
            PacketCodec.unit(new FlapPayload());

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}