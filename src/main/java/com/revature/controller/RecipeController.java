package com.revature.controller;

import io.javalin.http.Handler;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

import com.revature.model.Chef;
import com.revature.model.Recipe;
import com.revature.service.AuthenticationService;
import com.revature.service.RecipeService;

public class RecipeController {

    @SuppressWarnings("unused")
    private RecipeService recipeService;

    @SuppressWarnings("unused")
    private AuthenticationService authService;

    public RecipeController(RecipeService recipeService, AuthenticationService authService) {
        this.recipeService = recipeService;
        this.authService = authService;
    }

    // Strip "Bearer " or "Bearer" prefix from Authorization header
    private String extractToken(String header) {
        if (header == null) return null;
        if (header.startsWith("Bearer ")) return header.substring(7);
        if (header.startsWith("Bearer")) return header.substring(6);
        return header;
    }

    // Reads "name" param (unit tests) OR "term" param (integration tests)
    public Handler fetchAllRecipes = ctx -> {
        String term = ctx.queryParam("name") != null ? ctx.queryParam("name") : ctx.queryParam("term");

        Integer page = getParamAsClassOrElse(ctx, "page", Integer.class, null);
        Integer pageSize = getParamAsClassOrElse(ctx, "pageSize", Integer.class, null);
        String sortBy = ctx.queryParam("sortBy");
        String sortDirection = ctx.queryParam("sortDirection");

        if (page != null && pageSize != null) {
            ctx.status(200);
            ctx.json(recipeService.searchRecipes(term, page, pageSize, sortBy, sortDirection));
            return;
        }

        List<Recipe> recipes = recipeService.searchRecipes(term);

        if (recipes == null || recipes.isEmpty()) {
            ctx.status(404);
            ctx.result("No recipes found");
        } else {
            ctx.status(200);
            ctx.json(recipes);
        }
    };

    public Handler fetchRecipeById = ctx -> {
        int id = Integer.parseInt(ctx.pathParam("id"));
        recipeService.findRecipe(id)
            .ifPresentOrElse(
                recipe -> { ctx.status(200); ctx.json(recipe); },
                () -> { ctx.status(404); ctx.result("Recipe not found"); }
            );
    };

    public Handler createRecipe = ctx -> {
        String rawToken = ctx.header("Authorization");
        String token = extractToken(rawToken);

        Chef chef = authService.getChefFromSessionToken(token);
        if (chef == null) {
            ctx.status(401);
            return;
        }

        Recipe recipe = ctx.bodyAsClass(Recipe.class);

        // Always force a new insert regardless of id in request body
        recipe.setId(0);

        if (recipe.getAuthor() == null) {
            recipe.setAuthor(chef);
        }

        recipeService.saveRecipe(recipe);
        ctx.status(201);
    };

    public Handler deleteRecipe = ctx -> {
        int id = Integer.parseInt(ctx.pathParam("id"));

        if (recipeService.findRecipe(id).isEmpty()) {
            ctx.status(404);
            ctx.result("Recipe not found");
            return;
        }

        recipeService.deleteRecipe(id);
        ctx.status(200);
        ctx.result("Recipe deleted successfully");
    };

    public Handler updateRecipe = ctx -> {
        int id = Integer.parseInt(ctx.pathParam("id"));

        if (recipeService.findRecipe(id).isEmpty()) {
            ctx.status(404);
            ctx.result("Recipe not found");
            return;
        }

        Recipe recipe = ctx.bodyAsClass(Recipe.class);
        recipe.setId(id);
        recipeService.saveRecipe(recipe);

        ctx.status(200);
        ctx.json(recipe);
    };

    private <T> T getParamAsClassOrElse(Context ctx, String queryParam, Class<T> clazz, T defaultValue) {
        String paramValue = ctx.queryParam(queryParam);
        if (paramValue != null) {
            if (clazz == Integer.class) {
                return clazz.cast(Integer.valueOf(paramValue));
            } else if (clazz == Boolean.class) {
                return clazz.cast(Boolean.valueOf(paramValue));
            } else {
                return clazz.cast(paramValue);
            }
        }
        return defaultValue;
    }

    public void configureRoutes(Javalin app) {
        app.get("/recipes", fetchAllRecipes);
        app.get("/recipes/{id}", fetchRecipeById);
        app.post("/recipes", createRecipe);
        app.put("/recipes/{id}", updateRecipe);
        app.delete("/recipes/{id}", deleteRecipe);
    }
}
