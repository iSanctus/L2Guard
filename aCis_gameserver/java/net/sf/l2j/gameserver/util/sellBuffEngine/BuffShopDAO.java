package net.sf.l2j.gameserver.util.sellBuffEngine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.pool.ConnectionPool;

import net.sf.l2j.gameserver.enums.actors.Sex;
import net.sf.l2j.gameserver.model.actor.container.player.Appearance;

public class BuffShopDAO
{
	private static final Logger _log = Logger.getLogger(BuffShopDAO.class.getName());
	
	public void saveShop(ShopObject shop)
	{
		final String SAVE_SHOP = "REPLACE INTO buffshop (ownerId, buffs, title, store_message, x, y, z, heading, class_id, sex, face, hair_style, hair_color, equipped_items) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(SAVE_SHOP))
		{
			
			ps.setInt(1, shop.getOwnerId());
			ps.setString(2, shop.getBuffLine());
			ps.setString(3, shop.getTitle());
			ps.setString(4, shop.getStoreMessage());
			ps.setInt(5, shop.getX());
			ps.setInt(6, shop.getY());
			ps.setInt(7, shop.getZ());
			ps.setInt(8, shop.getHeading());
			ps.setInt(9, shop.getClassId());
			ps.setInt(10, shop.getAppearance().getSex().ordinal());
			ps.setInt(11, shop.getAppearance().getFace());
			ps.setInt(12, shop.getAppearance().getHairStyle());
			ps.setInt(13, shop.getAppearance().getHairColor());
			ps.setString(14, String.join(",", shop.getEquippedItems().stream().map(String::valueOf).toList()));
			
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "Error saving shop for owner " + shop.getOwnerId(), e);
		}
	}
	
	public List<ShopObject> loadShops()
	{
		final List<ShopObject> shops = new ArrayList<>();
		final String LOAD_SHOPS = "SELECT ownerId, buffs, title, store_message, x, y, z, heading, class_id, sex, face, hair_style, hair_color, equipped_items FROM buffshop";
		
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_SHOPS);
			ResultSet rs = ps.executeQuery())
		{
			
			while (rs.next())
			{
				// Cria��o do objeto de dados
				int ownerId = rs.getInt("ownerId");
				final ShopObject shop = new ShopObject(ownerId);
				
				// Populando o objeto com todos os dados da linha do DB
				shop.setTitle(rs.getString("title"));
				shop.setStoreMessage(rs.getString("store_message"));
				shop.setXYZ(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("heading"));
				shop.setClassId(rs.getInt("class_id"));
				shop.setAppearance(new Appearance((byte) rs.getInt("face"), (byte) rs.getInt("hair_color"), (byte) rs.getInt("hair_style"), Sex.VALUES[rs.getInt("sex")]));
				
				// Populando a lista de itens
				List<Integer> items = new ArrayList<>();
				String equipmentString = rs.getString("equipped_items");
				if (equipmentString != null && !equipmentString.isEmpty())
				{
					for (String itemId : equipmentString.split(","))
					{
						if (!itemId.trim().isEmpty())
							items.add(Integer.parseInt(itemId.trim()));
					}
				}
				shop.setEquippedItems(items);
				
				// ** PONTO CR�TICO: Populando a lista de buffs **
				String buffsString = rs.getString("buffs");
				if (buffsString != null && !buffsString.isEmpty())
				{
					for (String buffLine : buffsString.split(";"))
					{
						shop.addBuff(buffLine);
					}
				}
				
				// Log para confirmar que o objeto foi populado corretamente ANTES de sair do DAO
				_log.info(String.format("[DAO-LOAD] Carregado ShopObject para ownerId: %d. Total de buffs: %d.", ownerId, shop.getBuffList().size()));
				
				// Adiciona o objeto completo � lista de retorno
				shops.add(shop);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Erro ao carregar lojas de buff do banco de dados.", e);
		}
		return shops;
	}
	
	public void removeShop(int ownerId)
	{
		
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM buffshop WHERE ownerId=?"))
		{
			
			ps.setInt(1, ownerId);
			int affectedRows = ps.executeUpdate(); // Pega o n�mero de linhas afetadas
			
			if (affectedRows == 0)
			{
				_log.warning("BuffShopDAO: Tentativa de remover loja para ownerId " + ownerId + ", mas nenhuma linha foi encontrada/deletada.");
			}
			else
			{
				_log.info("BuffShopDAO: Loja para ownerId " + ownerId + " removida do banco de dados com sucesso.");
			}
			
		}
		catch (SQLException e)
		{
			// Log expl�cito e detalhado do erro
			_log.log(Level.SEVERE, "BuffShopDAO: Erro de SQL ao tentar remover a loja para ownerId " + ownerId, e);
		}
		catch (Exception e)
		{
			// Log para qualquer outro erro inesperado
			_log.log(Level.SEVERE, "BuffShopDAO: Erro inesperado ao remover a loja para ownerId " + ownerId, e);
		}
	}
	
	/**
	 * Adiciona uma quantia de adena a um jogador offline, atualizando diretamente o banco de dados.
	 * @param ownerId O ID do jogador a ser recompensado.
	 * @param adenaToAdd A quantidade de adena a ser adicionada.
	 */
	public void addAdenaToOfflinePlayer(int ownerId, int adenaToAdd)
	{
		// Evita chamadas desnecess�rias ao banco de dados se o valor for inv�lido.
		if (adenaToAdd <= 0)
		{
			return;
		}
		
		final String UPDATE_ADENA_QUERY = "UPDATE items SET count = count + ? WHERE item_id = 57 AND owner_id = ?";
		
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_ADENA_QUERY))
		{
			
			ps.setInt(1, adenaToAdd);
			ps.setInt(2, ownerId);
			ps.executeUpdate();
			
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "BuffShopDAO: Erro de SQL ao recompensar adena offline para ownerId " + ownerId, e);
		}
	}
	
}