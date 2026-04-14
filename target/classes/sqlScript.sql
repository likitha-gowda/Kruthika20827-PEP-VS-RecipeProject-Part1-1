-- Drop tables if they exist (for clean re-run)
DROP TABLE IF EXISTS RECIPE_INGREDIENT;
DROP TABLE IF EXISTS RECIPE;
DROP TABLE IF EXISTS INGREDIENT;
DROP TABLE IF EXISTS CHEF;

-- Create the Chef Table
CREATE TABLE CHEF (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_admin BOOLEAN
);

-- Create the Recipe Table
CREATE TABLE RECIPE (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    instructions VARCHAR(255) NOT NULL,
    chef_id INT,
    FOREIGN KEY (chef_id) REFERENCES CHEF(id) ON DELETE CASCADE
);

-- Create Ingredient Table
CREATE TABLE INGREDIENT (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL
);

-- Create Recipe_Ingredient Table
CREATE TABLE RECIPE_INGREDIENT (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipe_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    vol DECIMAL,
    unit VARCHAR(20) NOT NULL,
    is_metric BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (recipe_id) REFERENCES RECIPE(id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES INGREDIENT(id) ON DELETE CASCADE
);

-- Seed Chef data (ids 1-4)
INSERT INTO CHEF (id, username, email, password, is_admin) VALUES
(1, 'JoeCool', 'snoopy@null.com', 'redbarron', false),
(2, 'CharlieBrown', 'goodgrief@peanuts.com', 'thegreatpumpkin', false),
(3, 'RevaBuddy', 'revature@revature.com', 'codelikeaboss', false),
(4, 'ChefTrevin', 'trevin@revature.com', 'trevature', true);

-- Reset CHEF auto-increment sequence so next insert gets id=5
ALTER TABLE CHEF ALTER COLUMN id RESTART WITH 5;

-- Seed Recipe data (ids 1-5)
INSERT INTO RECIPE (id, name, instructions, chef_id) VALUES
(1, 'carrot soup', 'Put carrot in water.  Boil.  Maybe salt.', 1),
(2, 'potato soup', 'Put potato in water.  Boil.  Maybe salt.', 2),
(3, 'tomato soup', 'Put tomato in water.  Boil.  Maybe salt.', 2),
(4, 'lemon rice soup', 'Put lemon and rice in water.  Boil.  Maybe salt.', 4),
(5, 'stone soup', 'Put stone in water.  Boil.  Maybe salt.', 4);

-- Reset RECIPE auto-increment so next insert gets id=6
ALTER TABLE RECIPE ALTER COLUMN id RESTART WITH 6;

-- Seed Ingredient data (ids 1-6)
INSERT INTO INGREDIENT (id, name) VALUES
(1, 'carrot'),
(2, 'potato'),
(3, 'tomato'),
(4, 'lemon'),
(5, 'rice'),
(6, 'stone');

-- Reset INGREDIENT auto-increment so next insert gets id=7
ALTER TABLE INGREDIENT ALTER COLUMN id RESTART WITH 7;
