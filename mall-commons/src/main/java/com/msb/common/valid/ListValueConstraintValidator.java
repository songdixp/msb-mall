package com.msb.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;

public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {
    private final HashSet<Integer> hashSet = new HashSet<>();
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] val = constraintAnnotation.val();
        for (int i : val) {
            hashSet.add(i);
        }
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        System.out.println("ConstraintValidatorContext上下文对象:"+context.getDefaultConstraintMessageTemplate());
        return hashSet.contains(value);
    }
}
