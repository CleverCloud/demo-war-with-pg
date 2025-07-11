package com.example;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.logging.Logger;
import java.sql.DriverManager;
import java.sql.PreparedStatement;


@WebServlet("/dbcheck")
public class DbCheckServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DbCheckServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            InitialContext ctx = new InitialContext();

            String host = System.getenv("POSTGRESQL_ADDON_HOST");
            String port = System.getenv("POSTGRESQL_ADDON_PORT");
            String db = System.getenv("POSTGRESQL_ADDON_DB");
            String user = System.getenv("POSTGRESQL_ADDON_USER");
            String pass = System.getenv("POSTGRESQL_ADDON_PASSWORD");

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;

            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                LOGGER.info("✅ Connexion PostgreSQL réussie !");
                resp.getWriter().println("Connexion réussie");

                String sql = """
                CREATE TABLE IF NOT EXISTS log_test (
                    id SERIAL PRIMARY KEY,
                    message TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT now()
                );
                INSERT INTO log_test (message)
                VALUES (?)
                ON CONFLICT DO NOTHING;
                """;

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, "Connexion réussie à PostgreSQL");
                stmt.execute();
                
            }

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur de connexion à PostgreSQL : " + e.getMessage());
            try {
                resp.sendError(500, "Connexion échouée");
            } catch (Exception ignored) {}
        }
    }
}
