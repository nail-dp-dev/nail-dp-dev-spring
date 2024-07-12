package com.backend.naildp.validation;

import static com.backend.naildp.validation.ValidationGroups.*;

import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

@GroupSequence({Default.class, NotEmptyGroup.class, PatternCheckGroup.class,})
public interface ValidationSequence {
}
