package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.Player;

public class VoicedNameChange implements IVoicedCommandHandler {

    private static final String[] VOICED_COMMANDS = { "name_change" };

    @Override
    public boolean useVoicedCommand(String command, Player player, String params) {
        // Verifica se o comando tem parâmetro
        if (params == null || params.isEmpty()) {
            player.sendMessage("You must enter a valid name.");
            return false;
        }

        String newName = params.trim();

        // Validação de tamanho do nome
        if (newName.length() < 3 || newName.length() > 16) {
            player.sendMessage("The name must be between 3 and 16 characters.");
            return false;
        }

        // Opcional: checagem de caracteres válidos
        if (!newName.matches("[A-Za-z0-9]+")) {
            player.sendMessage("The name can only contain letters and numbers.");
            return false;
        }

        // Alterando o nome
        player.setName(newName);

        // Atualiza info do jogador para todos
        player.broadcastUserInfo();

        // Mensagem para o jogador
        player.sendMessage("Your name has been changed to " + newName);

        return true;
    }

    @Override
    public String[] getVoicedCommandList() {
        return VOICED_COMMANDS;
    }
}
