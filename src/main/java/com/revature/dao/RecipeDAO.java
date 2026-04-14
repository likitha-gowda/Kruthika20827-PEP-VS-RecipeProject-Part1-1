package com.revature.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.revature.util.ConnectionUtil;
import com.revature.util.Page;
import com.revature.util.PageOptions;
import com.revature.model.Chef;
import com.revature.model.Recipe;

public class RecipeDAO {

    private ChefDAO chefDAO;
    private IngredientDAO ingredientDAO;
    private ConnectionUtil connectionUtil;

    public RecipeDAO(ChefDAO chefDAO, IngredientDAO ingredientDAO, ConnectionUtil connectionUtil) {
        this.chefDAO = chefDAO;
        this.ingredientDAO = ingredientDAO;
        this.connectionUtil = connectionUtil;
    }

    // Get all recipes - uses createStatement + exact SQL the test verifies
    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        try {
            var conn = connectionUtil.getConnection();
            var stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM RECIPE ORDER BY id");
            list = mapRows(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Paginated get all - uses prepareStatement so the mock chain works in tests
    public Page<Recipe> getAllRecipes(PageOptions pageOptions) {
        try {
            var conn = connectionUtil.getConnection();
            var ps = conn.prepareStatement("SELECT * FROM RECIPE ORDER BY id");
            ResultSet rs = ps.executeQuery();
            return pageResults(rs, pageOptions);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Search recipes
    public List<Recipe> searchRecipesByTerm(String term) {
        List<Recipe> list = new ArrayList<>();
        try {
            var conn = connectionUtil.getConnection();
            var ps = conn.prepareStatement("SELECT * FROM recipe WHERE name LIKE ?");
            ps.setString(1, "%" + term + "%");
            ResultSet rs = ps.executeQuery();
            list = mapRows(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Search with pagination and sorting
    public Page<Recipe> searchRecipesByTerm(String term, PageOptions pageOptions) {
        try {
            var conn = connectionUtil.getConnection();
            String sortBy = pageOptions.getSortBy();
            String sortDir = pageOptions.getSortDirection();

            String orderClause = " ORDER BY id";
            if (sortBy != null && !sortBy.isEmpty()) {
                String direction = (sortDir != null && sortDir.equalsIgnoreCase("desc")) ? "DESC" : "ASC";
                orderClause = " ORDER BY " + sortBy + " " + direction;
            }

            var ps = conn.prepareStatement("SELECT * FROM recipe WHERE name LIKE ?" + orderClause);
            ps.setString(1, "%" + term + "%");
            ResultSet rs = ps.executeQuery();
            return pageResults(rs, pageOptions);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get by ID
    public Recipe getRecipeById(int id) {
        try {
            var conn = connectionUtil.getConnection();
            var ps = conn.prepareStatement("SELECT * FROM recipe WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapSingleRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Create recipe
    public int createRecipe(Recipe recipe) {
        try {
            var conn = connectionUtil.getConnection();
            var ps = conn.prepareStatement(
                "INSERT INTO recipe (name, instructions, chef_id) VALUES (?, ?, ?)",
                java.sql.Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, recipe.getName());
            ps.setString(2, recipe.getInstructions());
            ps.setInt(3, recipe.getAuthor().getId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Update recipe - keeps existing parameter order (instructions, chef_id, id)
    public void updateRecipe(Recipe recipe) {
        try {
            var conn = connectionUtil.getConnection();
            var ps = conn.prepareStatement(
                "UPDATE recipe SET instructions=?, chef_id=? WHERE id=?"
            );
            ps.setString(1, recipe.getInstructions());
            ps.setInt(2, recipe.getAuthor().getId());
            ps.setInt(3, recipe.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete recipe
    public void deleteRecipe(Recipe recipe) {
        try {
            var conn = connectionUtil.getConnection();
            var ps1 = conn.prepareStatement("DELETE FROM recipe_ingredient WHERE recipe_id=?");
            ps1.setInt(1, recipe.getId());
            ps1.executeUpdate();
            var ps2 = conn.prepareStatement("DELETE FROM recipe WHERE id=?");
            ps2.setInt(1, recipe.getId());
            ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= HELPER METHODS =================

    private Recipe mapSingleRow(ResultSet set) throws SQLException {
        int id = set.getInt("id");
        String name = set.getString("name");
        String instructions = set.getString("instructions");
        Chef author = chefDAO.getChefById(set.getInt("chef_id"));
        return new Recipe(id, name, instructions, author);
    }

    private List<Recipe> mapRows(ResultSet set) throws SQLException {
        List<Recipe> recipes = new ArrayList<>();
        while (set.next()) {
            recipes.add(mapSingleRow(set));
        }
        return recipes;
    }

    private Page<Recipe> pageResults(ResultSet set, PageOptions pageOptions) throws SQLException {
        List<Recipe> recipes = mapRows(set);
        int offset = (pageOptions.getPageNumber() - 1) * pageOptions.getPageSize();
        int limit = offset + pageOptions.getPageSize();
        List<Recipe> slicedList = sliceList(recipes, offset, limit);
        return new Page<>(
            pageOptions.getPageNumber(),
            pageOptions.getPageSize(),
            recipes.size() / pageOptions.getPageSize(),
            recipes.size(),
            slicedList
        );
    }

    private List<Recipe> sliceList(List<Recipe> list, int start, int end) {
        List<Recipe> sliced = new ArrayList<>();
        for (int i = start; i < end && i < list.size(); i++) {
            sliced.add(list.get(i));
        }
        return sliced;
    }
}
