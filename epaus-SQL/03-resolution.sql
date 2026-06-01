/*
 * ISEL-DEI-SisInf
 * ND 2022-2026
 *
 *   
 * Information Systems Project - Active Databases
 * Didactic material to support 
 * the Information Systems course
 * 
 *  * */

/* ### DO NOT CHANGE OR REMOVE THE MARKERS BELOW 
 * ### ONLY WRITE to THE TODO ZONE
 * ### */

CREATE OR REPLACE FUNCTION validate_NIF_func()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    -- Check if is NULL
    IF NEW.nif IS NULL THEN
        RAISE EXCEPTION 'NIF não pode ser NULL';
    END IF;

    -- Check size is exactly 9 digits
    IF length(NEW.nif) <> 9 THEN
        RAISE EXCEPTION 'NIF tem de conter exactamente 9 digitos';
    END IF;

    -- Check if all characters are digits
    IF NEW.nif !~ '^[0-9]+$' THEN
        RAISE EXCEPTION 'Tem de conter apenas digitos';
    END IF;

    RETURN NEW;
END;
$$;

-- region Question 1.a 
CREATE OR REPLACE TRIGGER validate_NIF
BEFORE INSERT OR UPDATE
ON cliente
FOR EACH ROW
EXECUTE FUNCTION validate_NIF_func();
-- endregion

CREATE OR REPLACE FUNCTION check_duplicated_contact_func()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    -- Checking duplicated for table contacto_mail
        IF tg_table_name = 'contacto_email' THEN

            -- Check mail
            IF NEW.email IS NOT NULL AND EXISTS (
                SELECT 1
                FROM contacto_email
                Where email = NEW.email
            ) THEN
                RAISE EXCEPTION 'Email $ já existe!';
            end if;
        end if;

    -- Checking duplicated for table contacto_telefone
    IF tg_table_name = 'contacto_telefone' THEN

        -- Check mail
        IF NEW.telefone IS NOT NULL AND EXISTS (
            SELECT 1
            FROM contacto_telefone
            Where telefone = NEW.telefone
        ) THEN
            RAISE EXCEPTION 'Telefone $ já existe!';
        end if;
    end if;

END;
$$;


-- region Question 1.b
CREATE OR REPLACE TRIGGER check_duplicated_contacts_trg
BEFORE INSERT OR UPDATE OF email
ON contacto_email
FOR EACH ROW
EXECUTE FUNCTION check_duplicated_contact_func();

CREATE OR REPLACE TRIGGER check_duplicated_contacts_trg
BEFORE INSERT OR UPDATE OF telefone
ON contacto_telefone
FOR EACH ROW
EXECUTE FUNCTION check_duplicated_contact_func();
--TODO
-- endregion

-- region Question 2
CREATE OR REPLACE FUNCTION fx_media_movel(days integer, instrumento_isin VARCHAR(12))
RETURNS NUMERIC
LANGUAGE plpgsql
AS $$
DECLARE
    media NUMERIC;
BEGIN
    SELECT AVG(valor_fecho)
    INTO media
    FROM valor_instrumento_diario vid
    WHERE vid.instrumento_isin = instrumento_isin AND data >= CURRENT_DATE - days;

    RETURN media;
END;
$$;
-- endregion

-- region Question 3
-- funcao ao para produzir a listagem de um portfolio, incluindo o ISIN, a quantidade, o
-- valor actual, e a percentagem de variacaoo em relacao ao dia anterior;

CREATE OR REPLACE FUNCTION fx-portefolio-info(portfolio_id BIGSERIAL)
RETURNS SETOF
LANGUAGE plpgsql
AS $$

END;
$$
--TODO
-- endregion

/*
-- region Question 4
CREATE OR REPLACE PROCEDURE p_actualizaValorDiario() ...
--TODO
-- endregion

-- region Question 5
CREATE OR REPLACE VIEW contacto_cliente(nif,carta_cidadao,nome,tipo_contacto,contacto,descricao)
AS
--TODO
-- endregion

-- region Other changes
--TODO
-- endregion
*/
