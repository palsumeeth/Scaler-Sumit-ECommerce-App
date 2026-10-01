# JPA

## Entities

### BaseModel

#### Q. Why do we not use `@Data` annotations on JPA entities?

The simple answer is because it causes severe memory consumption and performance issues.

##### 1. Equals and HashCode methods:

The `@Data` annotation generates `equals()` and `hashCode()` methods for your entity based on all fields. This can lead to several issues:

- **Performance:** If your entity has a large number of fields or fields that are collections, the `equals()` and `hashCode()` can become expensive to compute.

- **Cyclic References:** If you have bi-directional relationships (common in JPA entities), the generated `equals()` and `hashCode()` methods can cause `StackOverflowError` due to cyclic references. For example, if `EnitiyA` has a reference to `EntityB` and vice versa, calling `equals()` or `hashCode()` can lead to infinite recursion.

##### 2. ToString method:

The `@Data` annotation also generates a `toString()` method including all fields:

- **Performance:** Similar to `equals()` and `hashCode()`, generating a `toString()` representation of an entity with many fields or collections can be slow.

- **Sensitive Data Exposure:** Including all fields in the `toString()` method might inadvertently expose sensitive information, such as passwords or personal data, if the `toString()` method is logged or printed.

##### 3. Lazy Loading Issues:

JPA uses lazy loading for entity associations to optimize performance. However, the `@Data` annotation's generated methods can trigger lazy loading unintentionally.

- **Performance:** Accessing a lazily loaded collection or field in `equals()`, `hashCode()` or `toString()` can trigger a database query, leading to performance issues and unintended database access.

##### 4. Proxy Issues:

JPA entities are often proxied by Hibernate (or other JPA implementations) to manage lazy loading and other features. The `@Data` annotation can interfere with these proxies:

- **Initialization:** The `equals()` and `hashCode()` methods might rely on uninitialized fields when the entity is a proxy, leading to inconsistent or incorrect behavior.






### Q. Why do we need `@MappedBySuperClass`?

The `@MappedSuperclass` annotation in JPA (Java Persistence API) is used to designate a class whose mapping information
is applied to the entities that inherit from it. 
This is different from using the `@Entity` annotation, which defines a class as an entity that should be mapped to a database table.

In your case, using `@MappedSuperclass` is not appropriate alongside `@Entity` because they serve different purposes
and cannot be used together in the same class. Here's a detailed explanation:

#### Explanation

1. **@MappedSuperclass**: This annotation is used when you want to define common mapping information that can be
inherited by multiple entity classes. 
The superclass itself is not an entity and does not correspond to a table in the database. 
Instead, the properties and mappings defined in the superclass are inherited by subclasses that are actual entities.

2. **@Entity**: This annotation is used to define an actual entity that corresponds to a table in the database. 
The class marked with `@Entity` will be mapped to a database table, and instances of this class will be persisted to that table.

#### Q. Why Not Use Both?

Using both annotations together is incorrect because:
- `@Entity` implies that the class itself is an entity and should be mapped to a table.
- `@MappedSuperclass` implies that the class is not an entity by itself but provides common mapping information to subclasses.

Combining them would lead to confusion and errors because JPA needs to clearly understand whether it should create a table for the class or use it as a mapping superclass.

#### Correct Usage

If you want `BaseModel` to be a superclass that provides common fields to other entity classes, you should only use `@MappedSuperclass`. Here is the corrected code:

```java
package com.example.product_catalog_service.entity;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseModel {

    @Id
    private Long id;
}
```

Then, any subclass can extend `BaseModel` and be annotated with `@Entity` to be recognized as an entity:

```java
package com.example.product_catalog_service.entity;

import jakarta.persistence.Entity;

@Entity
public class Product extends BaseModel {
    private String name;
    private String description;

    // Getters and setters
}
```

In this setup:
- `BaseModel` is a mapped superclass, providing the `id` field to subclasses.
- `Product` is an entity that inherits the `id` field from `BaseModel` and adds its own fields.

#### Syntax Errors in IntelliJ

If you use `@Entity` and `@MappedSuperclass` together, IntelliJ IDEA will flag this as a syntax error because it violates
JPA's rules on class mapping. The corrected version, where `BaseModel` only uses `@MappedSuperclass` and subclasses use
`@Entity`, will resolve these syntax errors.

#### Summary

To summarize, `@MappedSuperclass` is used to create a superclass that provides common mapping information to its 
subclasses. These subclasses will be the actual entities mapped to database tables. 
Combining `@MappedSuperclass` with `@Entity` is incorrect and will lead to syntax errors and mapping issues. 
Instead, use only `@MappedSuperclass` in the superclass and `@Entity` in the subclasses.

---
## Fetch Types

1. Lazy 
2. Eager


## Fetch Modes

1. SELECT
2. SUBSELECT
3. JOIN




## Fetch Mode and Fetch Type in action


| No. | FetchType | FetchMode   | Scenario               | Queries            | DML |
|-----|-----------|-------------|------------------------|--------------------|-----|
| 1.  | _LAZY_    | _SELECT_    | Asked for Products     | 2 `SELECT` queries | 1.  |
|     |           |             |                        |                    | 2.  |
|     |           |             | Not Asked for Products | 1 `SELECT` query   |     |
| 2.  | _EAGER_   | _SELECT_    | Asked for Products     | 2 `SELECT` queries | 1.  |
|     |           |             |                        |                    | 2.  |
|     |           |             | Not Asked for Products | 2 `SELECT` queries | 1.  |
|     |           |             |                        |                    | 2.  |
| 3.  | _LAZY_    | _JOIN_      | Not asked for Products | 1 `JOIN` query     |     |
|     |           |             | Not Asked for Products | 1 `JOIN` query     |     |
| 4.  | _EAGER_   | _JOIN_      | Asked for Products     | 1 `JOIN` query     |     |
|     |           |             | Not Asked for Products | 1 `JOIN` query     |     |
| 5.  | _LAZY_    | _SUBSELECT_ | Asked for Products     | 2 `SELECT` query   | 1.  |
|     |           |             |                        |                    | 2.  |
|     |           |             | Not Asked for Products | 1 `SELECT` query   |     |
| 6.  | _EAGER_   | _SUBSELECT_ | Asked for Products     | 2 `SELECT` queries | 1.  |
|     |           |             |                        |                    | 2.  |
|     |           |             | Not Asked for Products | 2 `SELECT` queries | 1.  |
|     |           |             |                        |                    | 2.  |


### Key takeaways

1. When `FetchMode` was `SELECT` / `SUBSELECT`, `FetchType` is honored.
2. When `FetchMode` was `JOIN`, `FetchType` is ignored.



## ID Generation

The error you're encountering suggests that the ID generation strategy for the `Product` entity in your JPA Spring Boot application is not configured correctly. Specifically, Hibernate expects the ID to be generated automatically but finds that it is missing or not properly assigned.

Here are some commonly used ID generation strategies in JPA, and guidance on how to choose one:

### 1. **AUTO**
- **Description:** The persistence provider (Hibernate) will choose an appropriate strategy based on the database dialect.
- **Usage:** Often used as a default strategy.
- **Configuration:**
  ```java
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  ```

### 2. **IDENTITY**
- **Description:** The database is responsible for generating the ID, usually by using an auto-increment column.
- **Usage:** Suitable when you want the database to handle ID generation.
- **Configuration:**
  ```java
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  ```

### 3. **SEQUENCE**
- **Description:** A sequence object in the database is used to generate unique IDs.
- **Usage:** Useful when you want to control ID generation through a database sequence.
- **Configuration:**
  ```java
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
  @SequenceGenerator(name = "product_seq", sequenceName = "product_sequence")
  private Long id;
  ```

### 4. **TABLE**
- **Description:** A table in the database is used to simulate sequences, which can be useful for certain database types.
- **Usage:** Less common but provides a way to generate IDs using a dedicated table.
- **Configuration:**
  ```java
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "product_table")
  @TableGenerator(name = "product_table", table = "id_generator", pkColumnName = "gen_name", valueColumnName = "gen_value")
  private Long id;
  ```

### Recommendations

- **For simplicity and if you’re not using a specific database feature:** Use `GenerationType.AUTO`.
- **For databases that support auto-increment columns:** Use `GenerationType.IDENTITY`.
- **For databases that support sequences (e.g., PostgreSQL, Oracle):** Use `GenerationType.SEQUENCE`.
- **If you need to manage ID generation in a specific way or want to use a table for ID generation:** Use `GenerationType.TABLE`.

### Example

If you are using a MySQL database and want to use auto-increment IDs, you might configure your `Product` entity like this:

```java
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // other fields, getters, and setters
}
```

---

Make sure that the database schema matches the generation strategy you choose. For example, if you use `GenerationType.IDENTITY`, your database table should have an auto-increment column for the ID field.

If you are still encountering issues, ensure that there are no discrepancies between your JPA entity configuration and the actual database schema.


