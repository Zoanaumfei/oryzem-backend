package com.oryzem.backend.domain.aws.monthlybirthday;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.oryzem.backend.domain.aws.monthlybirthday.exception.MonthlyBirthdayNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyBirthdayService {

    private final MonthlyBirthdayRepository repository;

    public MonthlyBirthdayResponse createBirthday(MonthlyBirthdayRequest request) {
        MonthlyBirthday birthday = MonthlyBirthdayMapper.toDomain(request);

        validateDoesNotExist(birthday.getMonth(), birthday.getName());

        MonthlyBirthday saved = repository.save(birthday);
        return MonthlyBirthdayMapper.toResponse(saved, "Birthday saved successfully");
    }

    public MonthlyBirthdayResponse updateBirthday(MonthlyBirthdayRequest request) {
        MonthlyBirthday existing = repository
                .findById(request.getMonth(), request.getName())
                .orElseThrow(() -> new MonthlyBirthdayNotFoundException(
                        request.getMonth(), request.getName()
                ));

        MonthlyBirthday updated = MonthlyBirthdayMapper.toDomain(request);
        if (request.getPhotoKey() == null) {
            updated.setPhotoKey(existing.getPhotoKey());
        }

        MonthlyBirthday saved = repository.save(updated);
        return MonthlyBirthdayMapper.toResponse(saved, "Birthday updated successfully");
    }

    public MonthlyBirthdayResponse getBirthday(Integer month, String name) {
        MonthlyBirthday birthday = repository
                .findById(month, name)
                .orElseThrow(() -> new MonthlyBirthdayNotFoundException(month, name));
        return MonthlyBirthdayMapper.toResponse(birthday, "Birthday found");
    }

    public List<MonthlyBirthdayResponse> getBirthdaysByMonth(Integer month) {
        List<MonthlyBirthday> birthdays = repository.findAllByMonth(month);
        return birthdays.stream()
                .map(birthday -> MonthlyBirthdayMapper.toResponse(birthday, "Birthday listed"))
                .collect(Collectors.toList());
    }

    public List<MonthlyBirthdayResponse> getBirthdaysByNameContains(String name) {
        String normalizedName = normalizeName(name);
        List<MonthlyBirthday> birthdays = repository.findAllByNameContains(normalizedName);
        return birthdays.stream()
                .map(birthday -> MonthlyBirthdayMapper.toResponse(birthday, "Birthday listed"))
                .collect(Collectors.toList());
    }

    public List<MonthlyBirthdayResponse> getBirthdaysByMonthAndNameContains(Integer month, String name) {
        String normalizedName = normalizeName(name);
        List<MonthlyBirthday> birthdays = repository.findAllByMonthAndNameContains(month, normalizedName);
        return birthdays.stream()
                .map(birthday -> MonthlyBirthdayMapper.toResponse(birthday, "Birthday listed"))
                .collect(Collectors.toList());
    }

    public List<MonthlyBirthdayResponse> getAllBirthdays() {
        List<MonthlyBirthday> birthdays = repository.findAll();
        return birthdays.stream()
                .map(birthday -> MonthlyBirthdayMapper.toResponse(birthday, "Birthday listed"))
                .collect(Collectors.toList());
    }

    public void deleteBirthday(Integer month, String name) {
        MonthlyBirthday birthday = repository
                .findById(month, name)
                .orElseThrow(() -> new MonthlyBirthdayNotFoundException(month, name));
        repository.delete(birthday.getMonth(), birthday.getName());
    }

    private void validateDoesNotExist(Integer month, String name) {
        Optional<MonthlyBirthday> existing = repository.findById(month, name);
        if (existing.isPresent()) {
            throw new IllegalStateException(
                    String.format("Birthday %s/%s already exists", month, name)
            );
        }
    }

    private String normalizeName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is required");
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        return trimmed;
    }
}
