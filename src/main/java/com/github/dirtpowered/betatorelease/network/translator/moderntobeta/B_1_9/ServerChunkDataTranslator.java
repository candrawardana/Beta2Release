/*
 * Copyright (c) 2020 Dirt Powered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.dirtpowered.betatorelease.network.translator.moderntobeta.B_1_9;

import com.github.dirtpowered.betaprotocollib.packet.Version_B1_7.data.MapChunkPacketData;
import com.github.dirtpowered.betaprotocollib.packet.Version_B1_7.data.PreChunkPacketData;
import com.github.dirtpowered.betatorelease.BetaToRelease;
import com.github.dirtpowered.betatorelease.data.chunk.BetaChunk;
import com.github.dirtpowered.betatorelease.data.mappings.OldBlock;
import com.github.dirtpowered.betatorelease.data.mappings.PreFlatteningData;
import com.github.dirtpowered.betatorelease.network.client.ModernClient;
import com.github.dirtpowered.betatorelease.network.session.ServerSession;
import com.github.dirtpowered.betatorelease.network.translator.model.ModernToBeta;
import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;

public class ServerChunkDataTranslator implements ModernToBeta<ServerChunkDataPacket> {

    @Override
    public void translate(BetaToRelease main, ServerChunkDataPacket packet, ServerSession session, ModernClient modernClient) {
        Column chunkColumn = packet.getColumn();

        int chunkX = chunkColumn.getX();
        int chunkZ = chunkColumn.getZ();

        session.sendPacket(new PreChunkPacketData(chunkX, chunkZ, true));

        Chunk[] chunks = packet.getColumn().getChunks();

        BetaChunk betaChunk = new BetaChunk(chunkX, chunkZ);

        try {
            for (int i = 0; i < chunks.length; i++) {
                Chunk chunk = chunks[i];
                if (chunk == null || chunkColumn.getBiomeData().length == 0 /* skip non-full chunks for now */)
                    return;

                int xPos = chunkX * 16;
                int zPos = chunkZ * 16;

                final int columnCurrentHeight = i * 16;
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            int internalId = chunk.get(x, y, z).getId();
                            OldBlock oldBlock = PreFlatteningData.fromInternalId(internalId);

                            betaChunk.setBlock(x, y + columnCurrentHeight, z, oldBlock.getBlockId());
                            betaChunk.setMetaData(x, y + columnCurrentHeight, z, oldBlock.getBlockData());
                        }
                    }
                }

                session.sendPacket(new MapChunkPacketData(xPos, (short) 0, zPos, 15, 127, 15, betaChunk.serializeTileData()));
            }
        } catch (Exception e) {
            main.getLogger().error("unable to convert chunk at x: " + chunkX + ", z: " + chunkZ);
        }
    }
}
