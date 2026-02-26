package com.oryzem.backend.modules.vehicles.validation;

import com.oryzem.backend.modules.vehicles.dto.VehicleProjectUpsertRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class VehicleProjectValidator implements ConstraintValidator<ValidVehicleProject, VehicleProjectUpsertRequest> {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    @Override
    public boolean isValid(VehicleProjectUpsertRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean valid = true;
        valid = validateDate("me", "ME", request.getMe(), context) && valid;
        valid = validateDate("pvs", "PVS", request.getPvs(), context) && valid;
        valid = validateDate("s0", "S0", request.getS0(), context) && valid;
        valid = validateDate("sop", "SOP", request.getSop(), context) && valid;
        valid = validateDate("tppa", "TPPA", request.getTppa(), context) && valid;
        valid = validateDate("vff", "VFF", request.getVff(), context) && valid;

        valid = validateDate("bodyFramePvs", "bodyFrame_PVS", request.getBodyFramePvs(), context) && valid;
        valid = validateDate("bodyFrameS0", "bodyFrame_S0", request.getBodyFrameS0(), context) && valid;
        valid = validateDate("bodyFrameVff", "bodyFrame_VFF", request.getBodyFrameVff(), context) && valid;

        valid = validateDate("pvsEletTbt", "PVS_ELET_TBT", request.getPvsEletTbt(), context) && valid;
        valid = validateDate("s0EletTbt", "S0_ELET_TBT", request.getS0EletTbt(), context) && valid;
        valid = validateDate("vffEletTbt", "VFF_ELET_TBT", request.getVffEletTbt(), context) && valid;

        valid = validateDate("pvsM100", "PVS_M100", request.getPvsM100(), context) && valid;
        valid = validateDate("s0M100", "S0_M100", request.getS0M100(), context) && valid;
        valid = validateDate("vffM100", "VFF_M100", request.getVffM100(), context) && valid;

        valid = validateDate("pvsZp5Tbt", "PVS_ZP5_TBT", request.getPvsZp5Tbt(), context) && valid;
        valid = validateDate("s0Zp5Tbt", "S0_ZP5_TBT", request.getS0Zp5Tbt(), context) && valid;
        valid = validateDate("vffZp5Tbt", "VFF_ZP5_TBT", request.getVffZp5Tbt(), context) && valid;

        valid = validateDate("pvsZp7Tbt", "PVS_ZP7_TBT", request.getPvsZp7Tbt(), context) && valid;
        valid = validateDate("s0Zp7Tbt", "S0_ZP7_TBT", request.getS0Zp7Tbt(), context) && valid;
        valid = validateDate("vffZp7Tbt", "VFF_ZP7_TBT", request.getVffZp7Tbt(), context) && valid;

        return valid;
    }

    private boolean validateDate(String propertyName,
                                 String fieldLabel,
                                 String value,
                                 ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            LocalDate.parse(value, DATE_FORMAT);
            return true;
        } catch (DateTimeParseException ex) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(fieldLabel + " must be a valid YYYY-MM-DD")
                    .addPropertyNode(propertyName)
                    .addConstraintViolation();
            return false;
        }
    }
}
