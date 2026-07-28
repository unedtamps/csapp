CREATE TABLE user_details (
    id VARCHAR(26) PRIMARY KEY,
    address VARCHAR(255),
    phone_number VARCHAR(255),
    identification_number VARCHAR(255),
    date_of_birth VARCHAR(255),
    mother_name VARCHAR(255)
);

CREATE TABLE users (
    id VARCHAR(26) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(5) NOT NULL CHECK (role IN ('USER', 'ADMIN', 'STAFF')),
    details_id VARCHAR(26) UNIQUE,
    CONSTRAINT fk_user_details FOREIGN KEY (details_id) REFERENCES user_details(id)
);
