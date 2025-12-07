package com.kardexccpll.kardex.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class LoginDAO {

    @Autowired
    private DataSource dataSource;

    public boolean autenticar(String usuario, String contraseña) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE usuario = ? AND contraseña = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, contraseña);

            ResultSet rs = ps.executeQuery();
            rs.next();

            return rs.getInt(1) > 0;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


