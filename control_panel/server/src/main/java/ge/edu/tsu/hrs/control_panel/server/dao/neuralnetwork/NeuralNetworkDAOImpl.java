package ge.edu.tsu.hrs.control_panel.server.dao.neuralnetwork;

import ge.edu.tsu.hrs.control_panel.server.dao.DatabaseUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NeuralNetworkDAOImpl implements NeuralNetworkDAO {

    private PreparedStatement pstmt;

    @Override
    public void addNeuralNetwork(int id, byte[] data) {
        try {
            String sql = "INSERT INTO neural_network (id, data) VALUES (?,?)";
            pstmt = DatabaseUtil.getConnection().prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.setBytes(2, data);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseUtil.closeConnection();
        }
    }

    @Override
    public byte[] getNeuralNetworkData(int id) {
        try {
            String sql = "SELECT * FROM neural_network WHERE id = ?";
            pstmt = DatabaseUtil.getConnection().prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("data");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseUtil.closeConnection();
        }
        return null;
    }

    @Override
    public void deleteNeuralNetwork(int id) {
        try {
            String sql = "DELETE FROM neural_network WHERE id = ?";
            pstmt = DatabaseUtil.getConnection().prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseUtil.closeConnection();
        }
    }
}
