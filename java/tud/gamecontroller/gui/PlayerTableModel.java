/*
    Copyright (C) 2008-2012 Stephan Schiffel <stephan.schiffel@gmx.de>

    This file is part of GameController.

    GameController is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameController is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameController.  If not, see <http://www.gnu.org/licenses/>.
*/

package tud.gamecontroller.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import tud.gamecontroller.GDLVersion;
import tud.gamecontroller.game.impl.Game;
import tud.gamecontroller.players.LegalPlayerInfo;
import tud.gamecontroller.players.PlayerInfo;
import tud.gamecontroller.players.RandomPlayerInfo;
import tud.gamecontroller.players.RemotePlayerInfo;

public class PlayerTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -7703631717408478495L;

	private Game<?,?> game = null;
	private List<PlayerRecord> rows = null;

	public PlayerTableModel() {
		super();
		rows = new ArrayList<PlayerRecord>();
	}

	public void setGame(Game<?,?> game) {
		synchronized(this) {
			if (game != null) {
				while (rows.size() < game.getNumberOfRoles()) {
					rows.add(new PlayerRecord(rows.size()));
				}
			}
			if (game != null && (this.game == null || ! this.game.getGdlVersion().equals(game.getGdlVersion()) )) {
				for (PlayerRecord p:rows) {
					p.setGdlVersion(game.getGdlVersion());
				}
			}
			this.game = game;
		}
		this.fireTableDataChanged();
	}
	
	public int getColumnCount() {
		return 6; // role, type, gdlVersion, host, port, value
	}

	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex){
		case 0:
			return String.class;
		case 1:
			return PlayerType.class;
		case 2:
			return GDLVersion.class;
		case 3:
			return String.class;
		case 4:
			return Integer.class;
		case 5:
			return Integer.class;
		}
		return null;
	}

	public String getColumnName(int columnIndex) {
		switch(columnIndex){
		case 0:
			return "Role";
		case 1:
			return "Type";
		case 2:
			return "GDL Version";
		case 3:
			return "Host";
		case 4:
			return "Port";
		case 5:
			return "Value";
		}
		return null;
	}

	public int getRowCount() {
		return (game != null ? game.getNumberOfRoles() : 0); 
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex){
		case 0:
			return game.getRole(rowIndex).getKIFForm();
		case 1:
			return rows.get(rowIndex).getType().toString();
		case 2:
			return rows.get(rowIndex).getGdlVersion().toString();
		case 3:
			return (rows.get(rowIndex).getType().equals(PlayerType.REMOTE)?rows.get(rowIndex).getHost():"-");
		case 4:
			return (rows.get(rowIndex).getType().equals(PlayerType.REMOTE)?rows.get(rowIndex).getPort():0);
		case 5:
			return rows.get(rowIndex).getValue();
		}
		return null;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		switch(columnIndex){
		case 0:return false;
		case 1:return true;
		case 2:return true;
		case 3:return rows.get(rowIndex).getType().equals(PlayerType.REMOTE);
		case 4:return rows.get(rowIndex).getType().equals(PlayerType.REMOTE);
		case 5:return false;
		}
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch(columnIndex){
		case 0:
			break;
		case 1:
			rows.get(rowIndex).setType((PlayerType)aValue);
			break;
		case 2:
			rows.get(rowIndex).setGdlVersion((GDLVersion)aValue);
			break;
		case 3:
			rows.get(rowIndex).setHost((String)aValue);
			break;
		case 4:
			rows.get(rowIndex).setPort(((Integer)aValue).intValue());
			break;
		case 5:
			rows.get(rowIndex).setValue(((Integer)aValue).intValue());
			break;
		}
		super.setValueAt(aValue, rowIndex, columnIndex);
	}
	
	public List<PlayerRecord> getPlayerRecords(){
		// only return the visible rows
		// (there might be invisible ones if setGame is called with
		//  a game that has fewer roles than the previous one)
		return rows.subList(0, getRowCount()); 
	}

	public class PlayerRecord {
		
		private PlayerType type;
		private GDLVersion gdlVersion;
		private String host;
		private int port;
		private int value;
		private int row;

		public PlayerRecord(int row){
			this.type=PlayerType.RANDOM;
			this.gdlVersion= (game != null ? game.getGdlVersion() : GDLVersion.v1); // default GDL version is the GDL version of the game
			this.host="localhost";
			this.port=4001;
			this.value=-1;
			this.row=row;
		}
		
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
			fireTableCellUpdated(row, 3);
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
			fireTableCellUpdated(row, 4);
		}
		public PlayerType getType() {
			return type;
		}
		public void setType(PlayerType type) {
			this.type = type;
			fireTableRowsUpdated(row, row);
		}
		public GDLVersion getGdlVersion() {
			return gdlVersion;
		}
		public void setGdlVersion(GDLVersion gdlVersion) {
			this.gdlVersion = gdlVersion;
			fireTableCellUpdated(row, 2);
		}
		public int getValue() {
			return value;
		}
		public void setValue(int value) {
			this.value = value;
			fireTableCellUpdated(row, 4);
		}
		public PlayerInfo getPlayerInfo(){
			if(type.equals(PlayerType.REMOTE)){
				return new RemotePlayerInfo(row,host+":"+port,host,port,gdlVersion); 
			}else if(type.equals(PlayerType.RANDOM)){
				return new RandomPlayerInfo(row,gdlVersion);
			}else{
				return new LegalPlayerInfo(row,gdlVersion);
			}
		}
	}
}
