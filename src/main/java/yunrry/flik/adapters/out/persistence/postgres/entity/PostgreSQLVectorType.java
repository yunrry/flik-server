package yunrry.flik.adapters.out.persistence.postgres.entity;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PostgreSQLVectorType implements UserType<List<Double>> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<List<Double>> returnedClass() {
        return (Class<List<Double>>) (Class<?>) List.class;
    }

    @Override
    public boolean equals(List<Double> x, List<Double> y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(List<Double> x) throws HibernateException {
        return Objects.hashCode(x);
    }

    @Override
    public List<Double> nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {
        String vectorData = rs.getString(position);
        if (vectorData == null) {
            return null;
        }

        try {
            // PostgreSQL vector 형식: [0.1,0.2,0.3] 파싱
            String cleaned = vectorData.replace("[", "").replace("]", "");
            return Arrays.stream(cleaned.split(","))
                    .map(String::trim)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new HibernateException("Failed to parse vector data: " + vectorData, e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, List<Double> value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            try {
                String vectorString = "[" + value.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")) + "]";

                // PostgreSQL vector 타입으로 캐스팅
                st.setObject(index, vectorString, Types.OTHER);
            } catch (Exception e) {
                throw new HibernateException("Failed to set vector data", e);
            }
        }
    }

    @Override
    public List<Double> deepCopy(List<Double> value) throws HibernateException {
        return value == null ? null : value.stream().collect(Collectors.toList());
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(List<Double> value) throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    @Override
    public List<Double> assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy((List<Double>) cached);
    }

    @Override
    public List<Double> replace(List<Double> original, List<Double> target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}