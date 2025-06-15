ALTER TABLE `cab_booking_db`.`clients` 
ADD UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE,
ADD UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE,
ADD UNIQUE INDEX `credit card_UNIQUE` (`credit card` ASC) VISIBLE;
;
