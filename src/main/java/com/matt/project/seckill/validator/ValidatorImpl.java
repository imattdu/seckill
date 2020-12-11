package com.matt.project.seckill.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;


/**
 * @author matt
 * @create 2020-12-07 15:27
 */
@Component
public class ValidatorImpl implements InitializingBean {


    private Validator validator;

    public ValidationResult validate(Object bean){
        ValidationResult validationResult = new ValidationResult();
        Set<ConstraintViolation<ValidationResult>> validate = validator.validate(validationResult);

        if (validate.size() > 0) {
            validate.forEach(o ->{
                String message = o.getMessage();
                String propertyName = o.getPropertyPath().toString();
                validationResult.getErrorMsgMap().put(propertyName,message);
            });
        }

        return validationResult;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
