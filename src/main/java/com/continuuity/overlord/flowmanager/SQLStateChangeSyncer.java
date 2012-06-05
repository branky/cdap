package com.continuuity.overlord.flowmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * SQLStateChangeSyncer is called everytime there is a state change observed by {@link StateChangeListener}.
 * On callback, the {@link #process(StateChangeData)} gets called with {@link StateChangeData} that contains
 * information for the state of the flow.
 */
public class SQLStateChangeSyncer implements StateChangeCallback<StateChangeData>, Closeable {
  private static final Logger Log = LoggerFactory.getLogger(SQLStateChangeSyncer.class);
  private final Connection connection;

  public SQLStateChangeSyncer(String url) throws SQLException {
    connection = DriverManager.getConnection(url, "sa", "");
    initialization();
  }

  public Connection getConnection() {
    return connection;
  }

  public void initialization() {
    try {
      connection.prepareStatement(
       "CREATE TABLE flow_state ( timestamp INTEGER, account VARCHAR, application VARCHAR, flow VARCHAR, " +
         " payload VARCHAR, state INTEGER)"
      ).execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void process(StateChangeData data) {
    String sql = "INSERT INTO " +
      "flow_state (timestamp, account, application, flow, payload, state) " +
      " VALUES (?, ?, ?, ?, ?, ?);";
    try {
      PreparedStatement stmt = connection.prepareStatement(sql);
      stmt.setLong(1, data.getTimestamp()/1000);
      stmt.setString(2, data.getAccountId());
      stmt.setString(3, data.getApplication());
      stmt.setString(4, data.getFlowName());
      stmt.setString(5, data.getPayload());
      stmt.setInt(6, data.getType().getType());
      stmt.executeUpdate();
    } catch (SQLException e) {
      Log.error("Failed to write the state change to SQL DB (state : {}). Reason : {}", data.toString(), e.getMessage());
    }
  }

  /**
   * Closes this stream and releases any system resources associated
   * with it. If the stream is already closed then invoking this
   * method has no effect.
   *
   * @throws java.io.IOException if an I/O error occurs
   */
  @Override
  public void close() throws IOException {
    try {
      connection.close();
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }
}
