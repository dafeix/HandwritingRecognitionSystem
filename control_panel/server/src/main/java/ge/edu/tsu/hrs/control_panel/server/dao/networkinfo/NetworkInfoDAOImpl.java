package ge.edu.tsu.hrs.control_panel.server.dao.networkinfo;

import ge.edu.tsu.hrs.control_panel.model.network.CharSequence;
import ge.edu.tsu.hrs.control_panel.model.network.NetworkInfo;
import ge.edu.tsu.hrs.control_panel.model.network.NetworkProcessorType;
import ge.edu.tsu.hrs.control_panel.model.network.NetworkTrainingStatus;
import ge.edu.tsu.hrs.control_panel.model.network.TransferFunction;
import ge.edu.tsu.hrs.control_panel.server.dao.DatabaseUtil;
import ge.edu.tsu.hrs.control_panel.server.dao.normalizeddata.GroupedNormalizedDataDAO;
import ge.edu.tsu.hrs.control_panel.server.dao.normalizeddata.GroupedNormalizedDataDAOImpl;
import ge.edu.tsu.hrs.control_panel.server.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NetworkInfoDAOImpl implements NetworkInfoDAO {

    private PreparedStatement pstmt;

    private final GroupedNormalizedDataDAO groupedNormalizedDataDAO = new GroupedNormalizedDataDAOImpl();

    @Override
    public int addNetworkInfo(NetworkInfo networkInfo) {
        try {
            String sql = "INSERT INTO network_info (training_duration, weight_min_value," +
                    " weight_max_value, bias_min_value, bias_max_value, transfer_function_type, learning_rate, min_error, training_max_iteration," +
                    " number_of_training_data_in_one_iteration, char_sequence, hidden_layer, network_processor_type, network_meta_info, description," +
                    " training_status, current_squared_error, current_iterations) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
            pstmt = DatabaseUtil.getConnection().prepareStatement(sql);
            pstmt.setLong(1, networkInfo.getTrainingDuration());
            pstmt.setFloat(2, networkInfo.getWeightMinValue());
            pstmt.setFloat(3, networkInfo.getWeightMaxValue());
            pstmt.setFloat(4, networkInfo.getBiasMinValue());
            pstmt.setFloat(5, networkInfo.getBiasMaxValue());
            pstmt.setString(6, networkInfo.getTransferFunction().name());
            pstmt.setFloat(7, networkInfo.getLearningRate());
            pstmt.setFloat(8, networkInfo.getMinError());
            pstmt.setLong(9, networkInfo.getTrainingMaxIteration());
            pstmt.setLong(10, networkInfo.getNumberOfTrainingDataInOneIteration());
            pstmt.setString(11, networkInfo.getCharSequence().getCharactersRegex());
            pstmt.setString(12, StringUtil.getStringFromIntegerList(networkInfo.getHiddenLayer()));
            pstmt.setString(13, networkInfo.getNetworkProcessorType().name());
            pstmt.setString(14, networkInfo.getNetworkMetaInfo());
            pstmt.setString(15, networkInfo.getDescription());
            pstmt.setString(16, networkInfo.getTrainingStatus().name());
            pstmt.setFloat(17, networkInfo.getCurrentSquaredError());
            pstmt.setLong(18, networkInfo.getCurrentIterations());
            pstmt.executeUpdate();
            String idSql = "SELECT MAX(id) AS max_id FROM network_info";
            pstmt = DatabaseUtil.getConnection().prepareStatement(idSql);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int id = rs.getInt("max_id");
            System.out.println("Inserted network info with id - " + id);
            return id;
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseUtil.closeConnection();
        }
        return -1;
    }

    @Override
    public List<NetworkInfo> getNetworkInfoList(Integer id) {
        List<NetworkInfo> networkInfoList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM network_info WHERE 1 = 1 ";
            if (id != null) {
                sql += "AND id = '" + id + "' ";
            }
            sql += " ORDER BY id DESC;";
            pstmt = DatabaseUtil.getConnection().prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                NetworkInfo networkInfo = new NetworkInfo();
                networkInfo.setId(rs.getInt("id"));
                networkInfo.setTrainingDuration(rs.getLong("training_duration"));
                networkInfo.setWeightMinValue(rs.getFloat("weight_min_value"));
                networkInfo.setWeightMaxValue(rs.getFloat("weight_max_value"));
                networkInfo.setBiasMinValue(rs.getFloat("bias_min_value"));
                networkInfo.setBiasMaxValue(rs.getFloat("bias_max_value"));
                networkInfo.setTransferFunction(TransferFunction.valueOf(rs.getString("transfer_function_type")));
                networkInfo.setLearningRate(rs.getFloat("learning_rate"));
                networkInfo.setMinError(rs.getFloat("min_error"));
                networkInfo.setTrainingMaxIteration(rs.getLong("training_max_iteration"));
                networkInfo.setNumberOfTrainingDataInOneIteration(rs.getLong("number_of_training_data_in_one_iteration"));
                networkInfo.setCharSequence(new CharSequence(rs.getString("char_sequence")));
                networkInfo.setHiddenLayer(StringUtil.getIntegerListFromString(rs.getString("hidden_layer")));
                networkInfo.setNetworkProcessorType(NetworkProcessorType.valueOf(rs.getString("network_processor_type")));
                networkInfo.setNetworkMetaInfo(rs.getString("network_meta_info"));
                networkInfo.setDescription(rs.getString("description"));
                networkInfo.setTrainingStatus(NetworkTrainingStatus.valueOf(rs.getString("training_status")));
                networkInfo.setCurrentSquaredError(rs.getFloat("current_squared_error"));
                networkInfo.setCurrentIterations(rs.getLong("current_iterations"));
                networkInfoList.add(networkInfo);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseUtil.closeConnection();
        }
        return networkInfoList;
    }

    @Override
    public void deleteNetworkInfo(int id) {
        try {
            String sql = "DELETE fROM testing_info WHERE network_id = ?";
            pstmt = DatabaseUtil.getConnection().prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            sql = "DELETE FROM network_info WHERE id = ?";
            pstmt = DatabaseUtil.getConnection().prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseUtil.closeConnection();
        }
    }

    @Override
    public void updateTrainingCurrentState(float currentSquaredError, long currentIterations, long currentDuration, int id) {
        try {
            String sql = "UPDATE network_info SET current_squared_error = ?, current_iterations = ?, training_duration = ? WHERE id = ?";
            pstmt = DatabaseUtil.getConnection().prepareStatement(sql);
            pstmt.setFloat(1, currentSquaredError);
            pstmt.setLong(2, currentIterations);
            pstmt.setLong(3, currentDuration);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseUtil.closeConnection();
        }
    }

    @Override
    public void updateTrainedState(long trainingDuration, int id) {
        try {
            String sql = "UPDATE network_info SET training_status = ?, training_duration = ? WHERE id = ?";
            pstmt = DatabaseUtil.getConnection().prepareStatement(sql);
            pstmt.setString(1, NetworkTrainingStatus.TRAINED.name());
            pstmt.setLong(2, trainingDuration);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseUtil.closeConnection();
        }
    }

    @Override
    public CharSequence getCharSequenceById(int networkId) {
        try {
            String sql = "SELECT char_sequence FROM network_info WHERE id = ?";
            pstmt = DatabaseUtil.getConnection().prepareStatement(sql);
            pstmt.setInt(1, networkId);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return new CharSequence(rs.getString(1));
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseUtil.closeConnection();
        }
        return null;
    }

    @Override
    public void setFailedNetworkInfos() {
        try {
            String sql = "UPDATE network_info SET training_status = ? WHERE training_status = ?";
            pstmt = DatabaseUtil.getConnection().prepareStatement(sql);
            pstmt.setString(1, NetworkTrainingStatus.FAILED.name());
            pstmt.setString(2, NetworkTrainingStatus.TRAINING.name());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseUtil.closeConnection();
        }
    }
}
