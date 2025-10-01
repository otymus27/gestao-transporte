import { Role } from './role';

export class Usuario {
  id!: number; // Opcional, pois não existe em novos usuários
  username!: string;
  nome!: string;
  ativo!: boolean;
  roles!: Role[]; // ✅ Lista de proprietários
  // Opcional para manter compatibilidade com DTOs, mas **nunca retornado pelo backend** em GETs.
  // Será usado principalmente em UserCreateDTO ou UserUpdateDTO.
  password?: string;
}

