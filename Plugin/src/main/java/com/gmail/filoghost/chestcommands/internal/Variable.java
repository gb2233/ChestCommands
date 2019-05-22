/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.gmail.filoghost.chestcommands.internal;

import com.gmail.filoghost.chestcommands.bridge.EconomyBridge;
import com.gmail.filoghost.chestcommands.bridge.PlayerPointsBridge;
import com.gmail.filoghost.chestcommands.bridge.TokenManagerBridge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public enum Variable {

	PLAYER("{player}") {
		public String getReplacement(Player executor) {
			return executor.getName();
		}
	},

	ONLINE("{online}") {
		public String getReplacement(Player executor) {
			return String.valueOf(CachedGetters.getOnlinePlayers());
		}
	},

	POINTS("{points}") {
		public String getReplacement(Player executor) {
			if (PlayerPointsBridge.hasValidPlugin()) {
				return String.valueOf(PlayerPointsBridge.getPoints(executor));
			} else {
				return "[PLAYER POINTS PLUGIN NOT FOUND]";
			}
		}
	},

	TOKENS("{tokens}") {
		public String getReplacement(Player executor) {
			if (TokenManagerBridge.hasValidPlugin()) {
				return String.valueOf(TokenManagerBridge.getTokens(executor));
			} else {
				return "[TOKEN MANAGER PLUGIN NOT FOUND]";
			}
		}
	},

	MAX_PLAYERS("{max_players}") {
		public String getReplacement(Player executor) {
			return String.valueOf(Bukkit.getMaxPlayers());
		}
	},

	MONEY("{money}") {
		public String getReplacement(Player executor) {
			if (EconomyBridge.hasValidEconomy()) {
				return EconomyBridge.formatMoney(EconomyBridge.getMoney(executor));
			} else {
				return "[ECONOMY PLUGIN NOT FOUND]";
			}
		}
	},

	WORLD("{world}") {
		public String getReplacement(Player executor) {
			return executor.getWorld().getName();
		}
	};

	private String text;

	private Variable(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public abstract String getReplacement(Player executor);
}
