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

import co.aikar.taskchain.TaskChain;
import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.api.ClickHandler;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.OpenIconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.RefreshIconCommand;
import java.util.List;
import org.bukkit.entity.Player;

public class CommandsClickHandler implements ClickHandler {

  private List<IconCommand> commands;
  private boolean closeOnClick;

  public CommandsClickHandler(List<IconCommand> commands, boolean closeOnClick) {
    this.commands = commands;
    this.closeOnClick = closeOnClick;

    if (commands != null && !commands.isEmpty()) {
      for (IconCommand command : commands) {
        if (command instanceof OpenIconCommand || command instanceof RefreshIconCommand) {
          // Fix GUI closing if KEEP-OPEN is not set, and a command should open another GUI
          this.closeOnClick = false;
          break;
        }
      }
    }
  }

  @Override
  public boolean onClick(Player player) {
    if (commands != null && !commands.isEmpty()) {
      TaskChain taskChain = ChestCommands.getTaskChainFactory().newChain();

      for (IconCommand command : commands) {
        command.execute(player, taskChain);
      }

      taskChain.execute();
    }

    return closeOnClick;
  }

}
