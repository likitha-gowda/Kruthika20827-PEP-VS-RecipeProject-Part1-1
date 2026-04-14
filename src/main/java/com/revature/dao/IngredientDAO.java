package com.revature.dao;

import java.sql.*;
import java.util.*;

import com.revature.util.ConnectionUtil;
import com.revature.util.Page;
import com.revature.util.PageOptions;
import com.revature.model.Ingredient;

public class IngredientDAO {

    private ConnectionUtil connectionUtil;

    public IngredientDAO(ConnectionUtil connectionUtil) {
        this.connectionUtil = connectionUtil;
    }

    //  GET BY ID
    public Ingredient getIngredientById(int id) {
        try (Connection conn = connectionUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM INGREDIENT WHERE id = ?"
            );
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Ingredient(rs.getInt("id"), rs.getString("name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //  CREATE
    public int createIngredient(Ingredient ingredient) {
    try {
        var conn = connectionUtil.getConnection();

        var ps = conn.prepareStatement(
            "INSERT INTO ingredient (name) VALUES (?)",
            java.sql.Statement.RETURN_GENERATED_KEYS
        );

        ps.setString(1, ingredient.getName());

        int rows = ps.executeUpdate();

        if (rows == 0) return 0;

        ResultSet rs = ps.getGeneratedKeys();

        if (rs.next()) {
            int id = rs.getInt(1);
            ingredient.setId(id);   // ✅ VERY IMPORTANT
            return id;
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}
    //  DELETE
    public void deleteIngredient(Ingredient ingredient) {
        try (Connection conn = connectionUtil.getConnection()) {

            PreparedStatement ps1 = conn.prepareStatement(
                "DELETE FROM RECIPE_INGREDIENT WHERE ingredient_id = ?"
            );
            ps1.setInt(1, ingredient.getId());
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement(
                "DELETE FROM INGREDIENT WHERE id = ?"
            );
            ps2.setInt(1, ingredient.getId());
            ps2.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  UPDATE
    public void updateIngredient(Ingredient ingredient) {
        try (Connection conn = connectionUtil.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                "UPDATE INGREDIENT SET name = ? WHERE id = ?"
            );

            ps.setString(1, ingredient.getName());
            ps.setInt(2, ingredient.getId());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET ALL
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();

        try (Connection conn = connectionUtil.getConnection()) {
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM INGREDIENT ORDER BY id");

            while (rs.next()) {
                list.add(new Ingredient(rs.getInt("id"), rs.getString("name")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    //  PAGINATED GET ALL
    public Page<Ingredient> getAllIngredients(PageOptions options) {
        try (Connection conn = connectionUtil.getConnection()) {
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM INGREDIENT ORDER BY id");

            return pageResults(rs, options);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //  SEARCH LIST
    public List<Ingredient> searchIngredients(String term) {
        List<Ingredient> list = new ArrayList<>();

        try (Connection conn = connectionUtil.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM INGREDIENT WHERE LOWER(name) LIKE LOWER(?) ORDER BY id"
            );

            ps.setString(1, "%" + term + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Ingredient(rs.getInt("id"), rs.getString("name")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    //  SEARCH PAGINATED
    public Page<Ingredient> searchIngredients(String term, PageOptions options) {
    try (Connection conn = connectionUtil.getConnection()) {
        String sortBy = options.getSortBy();
        String sortDir = options.getSortDirection();

        String orderClause = " ORDER BY id";
        if (sortBy != null && !sortBy.isEmpty()) {
            String direction = (sortDir != null && sortDir.equalsIgnoreCase("desc")) ? "DESC" : "ASC";
            orderClause = " ORDER BY " + sortBy + " " + direction;
        }

        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM INGREDIENT WHERE LOWER(name) LIKE LOWER(?)" + orderClause
        );
        ps.setString(1, "%" + term + "%");
        ResultSet rs = ps.executeQuery();
        return pageResults(rs, options);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

    //  PAGINATION LOGIC
    private Page<Ingredient> pageResults(ResultSet rs, PageOptions options) throws SQLException {

        List<Ingredient> list = new ArrayList<>();

        while (rs.next()) {
            list.add(new Ingredient(rs.getInt("id"), rs.getString("name")));
        }

        int page = options.getPageNumber();
        int size = options.getPageSize();

        int from = (page - 1) * size;
        int to = Math.min(from + size, list.size());

        if (from >= list.size()) {
            return new Page<>(page, size, 0, list.size(), new ArrayList<>());
        }

        int totalPages = (int) Math.ceil((double) list.size() / size);

        return new Page<>(page, size, totalPages, list.size(), list.subList(from, to));
    }
}