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


-- region Question 1.a
CREATE OR REPLACE FUNCTION validate_NIF_func() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.nif IS NULL OR NEW.nif !~ '^[0-9]{9}$' THEN
        RAISE EXCEPTION 'NIF inválido! Tem de conter exatamente 9 dígitos numéricos.';
END IF;
RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER validate_NIF
    BEFORE INSERT OR UPDATE
    ON cliente
    FOR EACH ROW
EXECUTE FUNCTION validate_NIF_func();
-- endregion

-- region Question 1.b
CREATE OR REPLACE FUNCTION check_duplicated_contact_func()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    -- Verificar duplicado para a tabela contacto_email
    IF TG_TABLE_NAME = 'contacto_email' THEN
        IF (TG_OP = 'INSERT') OR (TG_OP = 'UPDATE' AND NEW.email IS DISTINCT FROM OLD.email) THEN
            IF EXISTS(
                    SELECT 1
                    FROM contacto_email
                    WHERE cliente_nif = NEW.cliente_nif
                      AND email = NEW.email
                ) THEN
                RAISE EXCEPTION 'Email % já existe para este cliente!', NEW.email;
            END IF;
        END IF;
    END IF;

    -- Verificar duplicado para a tabela contacto_telefone
    IF TG_TABLE_NAME = 'contacto_telefone' THEN
        IF (TG_OP = 'INSERT') OR (TG_OP = 'UPDATE' AND NEW.telefone IS DISTINCT FROM OLD.telefone) THEN
            IF EXISTS(
                    SELECT 1
                    FROM contacto_telefone
                    WHERE cliente_nif = NEW.cliente_nif
                      AND telefone = NEW.telefone
                ) THEN
                RAISE EXCEPTION 'Telefone % já existe para este cliente!', NEW.telefone;
            END IF;
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER check_duplicated_email_trg
    BEFORE INSERT OR UPDATE OF email, cliente_nif
    ON contacto_email
    FOR EACH ROW
EXECUTE FUNCTION check_duplicated_contact_func();

CREATE OR REPLACE TRIGGER check_duplicated_telefone_trg
    BEFORE INSERT OR UPDATE OF telefone, cliente_nif
    ON contacto_telefone
    FOR EACH ROW
EXECUTE FUNCTION check_duplicated_contact_func();
-- endregion

-- region Question 2
CREATE OR REPLACE FUNCTION fx_media_movel(days INTEGER, instrumento_isin VARCHAR(12))
    RETURNS NUMERIC
    LANGUAGE plpgsql
AS
$$
DECLARE
    media NUMERIC;
BEGIN
    -- Calcula a média móvel dos últimos N dias a partir dos registos diários do instrumento
    SELECT AVG(sub.valor_fecho)
    INTO media
    FROM (SELECT vid.valor_fecho
          FROM valor_instrumento_diario vid
          WHERE vid.instrumento_isin = fx_media_movel.instrumento_isin
          ORDER BY vid.data DESC
          LIMIT days) sub;

    RETURN media;
END;
$$;
-- endregion

-- region Question 3
CREATE OR REPLACE FUNCTION fx_portefolio_info(p_portefolio_id BIGINT)
    RETURNS TABLE
            (
                isin                       VARCHAR(12),
                quantidade                 NUMERIC,
                valor_actual               NUMERIC,
                percentagem_variacao_diaria NUMERIC
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    -- Devolve uma listagem com os dados requeridos, cruzando posições com dados fundamentais
    RETURN QUERY
        SELECT pos.instrumento_isin,
               pos.quantidade,
               df.valor_actual,
               df.percentagem_variacao_diaria
        FROM posicao AS pos
                 JOIN
             dados_fundamentais AS df ON pos.instrumento_isin = df.instrumento_isin
        WHERE pos.portefolio = p_portefolio_id;
END;
$$;
-- endregion

-- region Question 4
CREATE OR REPLACE PROCEDURE p_actualizaValorDiario()
    LANGUAGE plpgsql
AS
$$
DECLARE
    instrumento_rec RECORD;
    min_val         NUMERIC;
    max_val         NUMERIC;
    close_val       NUMERIC;
    open_val        NUMERIC;
BEGIN
    -- Loop por cada instrumento que teve triplos hoje
    FOR instrumento_rec IN
        SELECT DISTINCT identificador
        FROM triplo_externo
        WHERE data_tempo::DATE = CURRENT_DATE
        LOOP
            -- Descobrir o valor min e max de hoje
            SELECT MIN(valor), MAX(valor)
            INTO min_val, max_val
            FROM triplo_externo
            WHERE identificador = instrumento_rec.identificador
              AND data_tempo::DATE = CURRENT_DATE;

            -- Descobrir o valor de fecho
            SELECT valor
            INTO close_val
            FROM triplo_externo
            WHERE identificador = instrumento_rec.identificador
              AND data_tempo::DATE = CURRENT_DATE
            ORDER BY data_tempo DESC
            LIMIT 1;

            -- Descobrir o valor de abertura
            SELECT valor
            INTO open_val
            FROM triplo_externo
            WHERE identificador = instrumento_rec.identificador
              AND data_tempo::DATE = CURRENT_DATE
            ORDER BY data_tempo ASC
            LIMIT 1;

            -- Guardar tudo na tabela (Inserir ou Atualizar se já existir conflito em data)
            INSERT INTO valor_instrumento_diario (instrumento_isin, data, valor_minimo, valor_maximo, valor_abertura,
                                                  valor_fecho)
            VALUES (instrumento_rec.identificador, CURRENT_DATE, min_val, max_val, open_val, close_val)
            ON CONFLICT (instrumento_isin, data) DO UPDATE SET valor_minimo   = EXCLUDED.valor_minimo,
                                                                valor_maximo   = EXCLUDED.valor_maximo,
                                                                valor_abertura = EXCLUDED.valor_abertura,
                                                                valor_fecho    = EXCLUDED.valor_fecho;

        END LOOP;
END;
$$;
-- endregion

-- region Question 5
CREATE OR REPLACE VIEW contacto_cliente(nif, cartao_cidadao, nome, tipo_contacto, contacto, descricao)
AS
SELECT c.nif,
       c.cartao_cidadao,
       c.nome,
       'email'    AS tipo_contacto,
       ce.email   AS contacto,
       ce.descricao
FROM cliente c
         JOIN
     contacto_email ce ON c.nif = ce.cliente_nif
UNION ALL
SELECT c.nif,
       c.cartao_cidadao,
       c.nome,
       'telefone' AS tipo_contacto,
       ct.telefone  AS contacto,
       ct.descricao
FROM cliente c
         JOIN
     contacto_telefone ct ON c.nif = ct.cliente_nif;

CREATE OR REPLACE FUNCTION fn_contacto_cliente_dml()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    -- INSERT
    IF TG_OP = 'INSERT' THEN
        -- Verifica se o cliente já existe, se não, cria-o
        IF NOT EXISTS(SELECT 1 FROM cliente WHERE nif = NEW.nif) THEN
            INSERT INTO cliente(nif, cartao_cidadao, nome)
            VALUES (NEW.nif, NEW.cartao_cidadao, NEW.nome);
        END IF;

        -- Insere o contacto na tabela correspondente
        IF NEW.tipo_contacto = 'email' THEN
            INSERT INTO contacto_email(cliente_nif, email, descricao)
            VALUES (NEW.nif, NEW.contacto, NEW.descricao);
        ELSIF NEW.tipo_contacto = 'telefone' THEN
            INSERT INTO contacto_telefone(cliente_nif, telefone, descricao)
            VALUES (NEW.nif, NEW.contacto, NEW.descricao);
        ELSE
            RAISE EXCEPTION 'Tipo de contacto inválido: %', NEW.tipo_contacto;
        END IF;

        RETURN NEW;

        -- UPDATE
    ELSIF TG_OP = 'UPDATE' THEN
        -- Atualiza os dados do cliente
        UPDATE cliente
        SET nome           = NEW.nome,
            cartao_cidadao = NEW.cartao_cidadao
        WHERE nif = OLD.nif;

        -- Atualiza o contacto na tabela correspondente
        IF OLD.tipo_contacto = 'email' THEN
            UPDATE contacto_email
            SET email     = NEW.contacto,
                descricao = NEW.descricao
            WHERE cliente_nif = OLD.nif
              AND email = OLD.contacto;
        ELSIF OLD.tipo_contacto = 'telefone' THEN
            UPDATE contacto_telefone
            SET telefone  = NEW.contacto,
                descricao = NEW.descricao
            WHERE cliente_nif = OLD.nif
              AND telefone = OLD.contacto;
        END IF;

        RETURN NEW;

        -- DELETE
    ELSIF TG_OP = 'DELETE' THEN
        -- Remove o contacto na tabela correspondente
        IF OLD.tipo_contacto = 'email' THEN
            DELETE
            FROM contacto_email
            WHERE cliente_nif = OLD.nif
              AND email = OLD.contacto;
        ELSIF OLD.tipo_contacto = 'telefone' THEN
            DELETE
            FROM contacto_telefone
            WHERE cliente_nif = OLD.nif
              AND telefone = OLD.contacto;
        END IF;

        RETURN OLD;
    END IF;
END;
$$;

CREATE OR REPLACE TRIGGER trg_contacto_cliente_dml
    INSTEAD OF INSERT OR UPDATE OR DELETE
    ON contacto_cliente
    FOR EACH ROW
EXECUTE FUNCTION fn_contacto_cliente_dml();
-- endregion

-- region Other changes
-- Adiciona a coluna version à tabela cliente para suporte a Optimistic Locking em JPA (Questão 6.e)
ALTER TABLE cliente
    ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 0;

-- O sistema deve garantir que sempre que o valor diário
-- de um instrumento é alterado, é mantida coerência com o
-- valor diário registado para o mercado.
CREATE OR REPLACE FUNCTION update_market_value_func()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_mercado                  VARCHAR(20);
    v_sum_abertura             NUMERIC;
    v_ontem                    DATE;
    v_valor_abertura_mercado   NUMERIC;
BEGIN
    -- Obter o mercado associado ao instrumento modificado
    SELECT mercado INTO v_mercado FROM instrumento WHERE instrumento_id = NEW.instrumento_isin;

    -- Calcular a nova soma dos valores de abertura de todos os instrumentos desse mercado para o dia em causa
    SELECT SUM(valor_abertura)
    INTO v_sum_abertura
    FROM valor_instrumento_diario vid
             JOIN instrumento i ON i.instrumento_id = vid.instrumento_isin
    WHERE i.mercado = v_mercado
      AND vid.data = NEW.data;

    -- Obter o valor de índice do mercado do dia anterior
    v_ontem := NEW.data - 1;
    SELECT valor_indice
    INTO v_valor_abertura_mercado
    FROM valor_mercado_diario
    WHERE mercado = v_mercado
      AND data = v_ontem;

    IF v_valor_abertura_mercado IS NULL THEN
        v_valor_abertura_mercado := 0;
    END IF;

    -- Atualiza o valor diário do mercado
    INSERT INTO valor_mercado_diario(mercado, data, valor_indice, valor_abertura, variacao_diaria)
    VALUES (v_mercado, NEW.data, v_sum_abertura, v_valor_abertura_mercado,
            v_sum_abertura - v_valor_abertura_mercado)
    ON CONFLICT (mercado, data) DO UPDATE
        SET valor_indice   = EXCLUDED.valor_indice,
            valor_abertura = EXCLUDED.valor_abertura,
            variacao_diaria = EXCLUDED.variacao_diaria;

    RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER update_market_value_trg
    AFTER INSERT OR UPDATE
    ON valor_instrumento_diario
    FOR EACH ROW
EXECUTE FUNCTION update_market_value_func();
-- endregion