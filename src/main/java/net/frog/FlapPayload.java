package net.frog;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FlapPayload() implements CustomPacketPayload {
    public static final Type<FlapPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("icarus-wings", "flap"));
    public static final StreamCodec<FriendlyByteBuf, FlapPayload> CODEC =
            StreamCodec.unit(new FlapPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}