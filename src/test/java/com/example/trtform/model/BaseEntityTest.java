package com.example.trtform.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseEntityTest {

    @Test
    void allEntitiesShouldShareABaseEntityWithId() {
        assertTrue(BaseEntity.class.isAssignableFrom(User.class));
        assertTrue(BaseEntity.class.isAssignableFrom(Survey.class));
        assertTrue(BaseEntity.class.isAssignableFrom(Question.class));
        assertTrue(BaseEntity.class.isAssignableFrom(Participation.class));
    }
}
