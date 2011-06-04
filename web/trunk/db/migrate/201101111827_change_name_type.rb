class ChangeNameType < ActiveRecord::Migration
  def self.up
    change_column(:city_names, :name, :string)
  end

  def self.down
    change_column(:city_names, :name, :integer)
  end
end
