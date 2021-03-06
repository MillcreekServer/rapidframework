/*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.rapidframework3.bukkit.events;

import io.github.wysohn.rapidframework3.data.SimpleChunkLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * This event fires depends on the player's block location. Unlike the
 * PlayerMoveEvent, it only checks whether a player moved from a chunk to
 * another chunk. This significantly reduces the server load when you want to
 * check player entering area, etc.
 *
 * @author wysohn
 */
public class PlayerChunkLocationEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final SimpleChunkLocation from;
    private final SimpleChunkLocation to;

    public PlayerChunkLocationEvent(Player who, SimpleChunkLocation from, SimpleChunkLocation to) {
        super(who);
        this.from = from;
        this.to = to;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public SimpleChunkLocation getFrom() {
        return from;
    }

    public SimpleChunkLocation getTo() {
        return to;
    }

}
